package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.TestSummaryDTO;
import com.finalyearproject.fyp.service.AiTestService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AiTestController {

    private final AiTestService aiTestService;

    /**
     * POST /your-courses/{courseId}/chapters/{chapterId}/tests/generate
     * Body: { "testTitle": "...", "questionCount": 5 }
     * Available to both teachers and students.
     */
    @PostMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/generate")
    @ResponseBody
    public ResponseEntity<?> generateTest(
            @PathVariable Long courseId,
            @PathVariable Long chapterId,
            @RequestBody Map<String, Object> payload,
            Authentication auth) {
        try {
            String email         = YourCoursesController.extractEmail(auth);
            String title         = (String) payload.getOrDefault("testTitle", "");
            int    questionCount = payload.get("questionCount") != null
                    ? Integer.parseInt(payload.get("questionCount").toString()) : 5;

            TestSummaryDTO result = aiTestService.generateTestFromChapter(
                    courseId, chapterId, email, title, questionCount);

            return ResponseEntity.ok(Map.of(
                    "testId",         result.testId(),
                    "testTitle",      result.testTitle(),
                    "questionCount",  result.questionCount(),
                    "message",        "Test generated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}