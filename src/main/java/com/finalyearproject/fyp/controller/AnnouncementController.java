package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.AnnouncementDTO;
import com.finalyearproject.fyp.dto.CreateAnnouncementRequest;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.repository.AnnouncementRepository;
import com.finalyearproject.fyp.repository.ChapterRepository;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.service.AnnouncementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService    announcementService;
    private final CourseRepository       courseRepository;
    private final ChapterRepository      chapterRepository;
    private final AnnouncementRepository announcementRepository;

    // Announcements page for a course

    @GetMapping("/your-courses/{courseId}/announcements")
    public String announcementsPage(@PathVariable Long courseId,
                                    Model model,
                                    HttpServletRequest request,
                                    Authentication auth) {

        Course               course        = getCourse(courseId);
        List<AnnouncementDTO> announcements = announcementService.getAnnouncementsForCourse(courseId);

        List<Map<String, Object>> chapterData = chapterRepository
                .findByCourseOrderByChapterOrderAsc(course)
                .stream()
                .map(ch -> Map.<String, Object>of(
                        "chapterId",    ch.getChapterId(),
                        "chapterTitle", ch.getChapterTitle() != null ? ch.getChapterTitle() : ""
                ))
                .toList();

        String email = YourCoursesController.extractEmail(auth);

        model.addAttribute("course",        course);
        model.addAttribute("announcements", announcements);
        model.addAttribute("chapterData",   chapterData);
        model.addAttribute("teacherEmail",  email);
        model.addAttribute("currentPath",   request.getRequestURI());
        return "announcements/index";
    }


    @GetMapping("/announcement/{announcementId}/redirect")
    public String announcementRedirect(@PathVariable Long announcementId) {
        return announcementRepository.findById(announcementId)
                .map(a -> "redirect:/your-courses/" + a.getCourse().getCourseId() + "/announcements")
                .orElse("redirect:/your-courses");
    }

    // Create announcement (teacher only)

    @PostMapping("/your-courses/{courseId}/announcements")
    @ResponseBody
    public ResponseEntity<?> createAnnouncement(@PathVariable Long courseId,
                                                @RequestBody CreateAnnouncementRequest request,
                                                Authentication auth) {
        try {
            String email  = YourCoursesController.extractEmail(auth);
            AnnouncementDTO result = announcementService.createAnnouncement(courseId, email, request);
            return ResponseEntity.ok(result);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Delete announcement (teacher/author only)

    @DeleteMapping("/your-courses/{courseId}/announcements/{announcementId}")
    @ResponseBody
    public ResponseEntity<?> deleteAnnouncement(@PathVariable Long courseId,
                                                @PathVariable Long announcementId,
                                                Authentication auth) {
        try {
            String email = YourCoursesController.extractEmail(auth);
            announcementService.deleteAnnouncement(announcementId, email);
            return ResponseEntity.ok(Map.of("message", "Announcement deleted."));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    // helper

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }
}