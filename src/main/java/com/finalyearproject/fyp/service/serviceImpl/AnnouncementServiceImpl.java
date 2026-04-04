package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.AnnouncementDTO;
import com.finalyearproject.fyp.dto.AnnouncementDTO.ContentSegment;
import com.finalyearproject.fyp.dto.CreateAnnouncementRequest;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.AnnouncementService;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementRepository            announcementRepository;
    private final AnnouncementChapterTagRepository  chapterTagRepository;
    private final CourseRepository                  courseRepository;
    private final ChapterRepository                 chapterRepository;
    private final UserRepository                    userRepository;
    private final UserCourseRepository              userCourseRepository;
    private final UserCourseEnrollmentRepository    enrollmentRepository;
    private final NotificationService               notificationService;

    // Get all announcements for a course

    @Override
    public List<AnnouncementDTO> getAnnouncementsForCourse(Long courseId) {
        Course course = getCourse(courseId);
        return announcementRepository.findByCourseOrderByCreatedAtDesc(course)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // Create announcement

    @Override
    @Transactional
    public AnnouncementDTO createAnnouncement(Long courseId,
                                              String teacherEmail,
                                              CreateAnnouncementRequest request) {
        Course course = getCourse(courseId);
        User   author = getUser(teacherEmail);

        // Verify the teacher actually owns this course
        boolean isTeacher = userCourseRepository.findByCourse(course)
                .stream()
                .anyMatch(uc -> uc.getUser().getUserId().equals(author.getUserId()));
        if (!isTeacher) {
            throw new SecurityException("You are not the teacher of this course.");
        }

        Announcement announcement = new Announcement();
        announcement.setCourse(course);
        announcement.setAuthor(author);
        announcement.setTitle(request.title());
        announcement.setContent(request.content());
        announcement.setCreatedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        announcementRepository.save(announcement);

        // Save chapter tags
        if (request.taggedChapterIds() != null) {
            for (Long chapterId : request.taggedChapterIds()) {
                chapterRepository.findById(chapterId).ifPresent(chapter -> {
                    AnnouncementChapterTag tag = new AnnouncementChapterTag();
                    tag.setAnnouncementId(announcement.getAnnouncementId());
                    tag.setChapterId(chapterId);
                    chapterTagRepository.save(tag);
                });
            }
        }

        // Notify all enrolled students
        List<Long> enrolledStudentIds = enrollmentRepository.findByCourse(course)
                .stream()
                .map(uce -> uce.getUser().getUserId())
                .collect(Collectors.toList());

        if (!enrolledStudentIds.isEmpty()) {
            notificationService.sendToUsers(
                    enrolledStudentIds,
                    "New announcement in " + course.getCourseName(),
                    author.getUsername() + " posted: \"" + request.title() + "\"",
                    "ANNOUNCEMENT",
                    announcement.getAnnouncementId()
            );
        }

        return toDTO(announcement);
    }

    // Delete announcement

    @Override
    @Transactional
    public void deleteAnnouncement(Long announcementId, String teacherEmail) {
        Announcement announcement = getAnnouncement(announcementId);
        User caller = getUser(teacherEmail);

        if (!announcement.getAuthor().getUserId().equals(caller.getUserId())) {
            throw new SecurityException("You can only delete your own announcements.");
        }

        announcementRepository.delete(announcement);
    }

    // Private helpers

    private AnnouncementDTO toDTO(Announcement a) {
        // Build a map of lowercase chapterTitle → Chapter for fast lookup
        Map<String, Chapter> titleToChapter = a.getChapterTags() == null ? new HashMap<>() :
                a.getChapterTags().stream()
                        .filter(t -> t.getChapter() != null)
                        .collect(Collectors.toMap(
                                t -> t.getChapter().getChapterTitle().toLowerCase(),
                                AnnouncementChapterTag::getChapter,
                                (first, second) -> first
                        ));

        List<ContentSegment> segments = parseContent(
                a.getContent(), titleToChapter, a.getCourse().getCourseId()
        );

        return new AnnouncementDTO(
                a.getAnnouncementId(),
                a.getCourse().getCourseId(),
                a.getCourse().getCourseName(),
                a.getAuthor().getUsername(),
                a.getAuthor().getUserId(),
                a.getTitle(),
                a.getContent(),
                a.getCreatedAt(),
                segments
        );
    }

    private List<ContentSegment> parseContent(String content,
                                              Map<String, Chapter> titleToChapter,
                                              Long courseId) {
        List<ContentSegment> segments = new ArrayList<>();
        if (content == null || content.isBlank()) return segments;

        // Sort chapter titles longest-first so that longer titles are matched
        // before shorter ones that might be a prefix of a longer name.
        // e.g. "Chapter 10" matched before "Chapter 1".
        List<String> sortedTitles = new ArrayList<>(titleToChapter.keySet());
        sortedTitles.sort(Comparator.comparingInt(String::length).reversed());

        StringBuilder plainBuffer = new StringBuilder();
        int i = 0;

        while (i < content.length()) {
            if (content.charAt(i) == '#') {
                // Try to match a chapter title starting at i+1
                String rest          = content.substring(i + 1);
                String matchedTitle  = null;
                Chapter matchedChapter = null;

                for (String title : sortedTitles) {
                    // Case-insensitive match at the start of 'rest'
                    if (rest.toLowerCase().startsWith(title)) {
                        // Make sure the match ends at a word boundary
                        // (end of string, or followed by a non-letter/non-digit/non-space char,
                        //  or followed by whitespace that is NOT part of the chapter title itself)
                        int endPos = title.length();
                        boolean boundaryOk = (endPos >= rest.length())
                                || !Character.isLetterOrDigit(rest.charAt(endPos));
                        if (boundaryOk) {
                            matchedTitle   = title;
                            matchedChapter = titleToChapter.get(title);
                            break;
                        }
                    }
                }

                if (matchedChapter != null) {
                    // Flush accumulated plain text (strip surrounding blank lines)
                    if (!plainBuffer.isEmpty()) {
                        segments.add(new ContentSegment(
                                stripSurroundingBlankLines(plainBuffer.toString()), null, null
                        ));
                        plainBuffer.setLength(0);
                    }
                    // Emit the chapter link segment using the original casing from the entity
                    segments.add(new ContentSegment(
                            "#" + matchedChapter.getChapterTitle(),
                            matchedChapter.getChapterId(),
                            courseId
                    ));
                    // Advance past the '#' + matched title
                    i += 1 + matchedTitle.length();
                } else {
                    // '#' with no matching title — treat as plain text
                    plainBuffer.append('#');
                    i++;
                }
            } else {
                plainBuffer.append(content.charAt(i));
                i++;
            }
        }

        // Flush remaining plain text
        if (!plainBuffer.isEmpty()) {
            segments.add(new ContentSegment(
                    stripSurroundingBlankLines(plainBuffer.toString()), null, null
            ));
        }

        return segments;
    }

    private String stripSurroundingBlankLines(String text) {
        // Split on newlines, drop leading/trailing empty lines, rejoin
        String[] lines = text.split("\n", -1);
        int start = 0;
        int end   = lines.length - 1;
        while (start <= end && lines[start].isBlank()) start++;
        while (end >= start && lines[end].isBlank())   end--;
        if (start > end) return "";
        return String.join("\n", Arrays.copyOfRange(lines, start, end + 1));
    }

    private Course       getCourse(Long id)       { return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found: " + id)); }
    private User         getUser(String email)    { return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: " + email)); }
    private Announcement getAnnouncement(Long id) { return announcementRepository.findById(id).orElseThrow(() -> new RuntimeException("Announcement not found: " + id)); }
}