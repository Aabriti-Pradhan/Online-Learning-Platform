package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterRepository            chapterRepository;
    private final CourseRepository             courseRepository;
    private final UserCourseResourceRepository ucrRepository;
    private final UserCourseTestRepository     uctRepository;

    /** Create a new chapter inside a course */
    @PostMapping("/your-courses/{courseId}/chapters")
    public ResponseEntity<?> createChapter(@PathVariable Long courseId,
                                           @RequestBody Map<String, String> payload) {
        Course course = getCourse(courseId);

        int order = chapterRepository.countByCourse(course) + 1;

        Chapter chapter = new Chapter();
        chapter.setCourse(course);
        chapter.setChapterTitle(payload.getOrDefault("chapterTitle", "Untitled Chapter"));
        chapter.setChapterDesc(payload.getOrDefault("chapterDesc", ""));
        chapter.setChapterOrder(order);
        chapter.setCreatedAt(LocalDateTime.now());
        chapterRepository.save(chapter);

        return ResponseEntity.ok(Map.of(
                "chapterId",    chapter.getChapterId(),
                "chapterTitle", chapter.getChapterTitle(),
                "chapterOrder", chapter.getChapterOrder()
        ));
    }

    /** Update chapter title/description */
    @PutMapping("/your-courses/{courseId}/chapters/{chapterId}")
    public ResponseEntity<?> updateChapter(@PathVariable Long courseId,
                                           @PathVariable Long chapterId,
                                           @RequestBody Map<String, String> payload) {
        Chapter chapter = getChapter(chapterId);
        if (payload.containsKey("chapterTitle")) chapter.setChapterTitle(payload.get("chapterTitle"));
        if (payload.containsKey("chapterDesc"))  chapter.setChapterDesc(payload.get("chapterDesc"));
        chapterRepository.save(chapter);
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    /** Delete a chapter and all its resources/tests */
    @DeleteMapping("/your-courses/{courseId}/chapters/{chapterId}")
    @Transactional
    public ResponseEntity<?> deleteChapter(@PathVariable Long courseId,
                                           @PathVariable Long chapterId) {
        Chapter chapter = getChapter(chapterId);
        // Resources and tests are cascade-deleted via the Chapter entity's cascade settings
        chapterRepository.delete(chapter);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Course getCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));
    }

    private Chapter getChapter(Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterId));
    }
}
