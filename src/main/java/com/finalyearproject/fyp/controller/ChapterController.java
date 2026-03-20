package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.ChapterDTO;
import com.finalyearproject.fyp.dto.CreateChapterRequest;
import com.finalyearproject.fyp.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    @PostMapping("/your-courses/{courseId}/chapters")
    public ResponseEntity<ChapterDTO> createChapter(@PathVariable Long courseId,
                                                    @RequestBody CreateChapterRequest request) {
        return ResponseEntity.ok(chapterService.createChapter(courseId, request));
    }

    @PutMapping("/your-courses/{courseId}/chapters/{chapterId}")
    public ResponseEntity<?> updateChapter(@PathVariable Long courseId,
                                           @PathVariable Long chapterId,
                                           @RequestBody CreateChapterRequest request) {
        chapterService.updateChapter(chapterId, request);
        return ResponseEntity.ok(Map.of("message", "Updated"));
    }

    @DeleteMapping("/your-courses/{courseId}/chapters/{chapterId}")
    public ResponseEntity<?> deleteChapter(@PathVariable Long courseId,
                                           @PathVariable Long chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}