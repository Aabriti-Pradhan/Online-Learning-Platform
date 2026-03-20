package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.*;
import com.finalyearproject.fyp.entity.Chapter;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.Test;
import com.finalyearproject.fyp.repository.ChapterRepository;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.service.TestService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final TestService       testService;
    private final CourseRepository  courseRepository;
    private final ChapterRepository chapterRepository;

    // Tests dashboard

    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/tests")
    public String testsDashboard(@PathVariable Long courseId,
                                 @PathVariable Long chapterId,
                                 Model model, Authentication auth,
                                 HttpServletRequest request) {
        Course  course    = getCourse(courseId);
        Chapter chapter   = getChapter(chapterId);
        boolean isTeacher = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        model.addAttribute("course",      course);
        model.addAttribute("chapter",     chapter);
        model.addAttribute("isTeacher",   isTeacher);
        model.addAttribute("currentPath", request.getRequestURI());

        if (isTeacher) {
            model.addAttribute("testSummaries", testService.getTestsForTeacher(chapterId));
            return "tests/index";
        } else {
            String email = YourCoursesController.extractEmail(auth);
            model.addAttribute("testData", testService.getTestsForStudent(chapterId, email));
            return "tests/take";
        }
    }

    // Create test

    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/create")
    public String createTestPage(@PathVariable Long courseId,
                                 @PathVariable Long chapterId,
                                 Model model) {
        model.addAttribute("course",  getCourse(courseId));
        model.addAttribute("chapter", getChapter(chapterId));
        return "tests/create";
    }

    @PostMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/create")
    @ResponseBody
    public ResponseEntity<TestSummaryDTO> saveTest(@PathVariable Long courseId,
                                                   @PathVariable Long chapterId,
                                                   @RequestBody CreateTestRequest request,
                                                   Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return ResponseEntity.ok(testService.createTest(courseId, chapterId, email, request));
    }

    // Delete test

    @DeleteMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/{testId}")
    @ResponseBody
    public ResponseEntity<?> deleteTest(@PathVariable Long courseId,
                                        @PathVariable Long chapterId,
                                        @PathVariable Long testId) {
        testService.deleteTest(courseId, testId);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // Teacher: per-student results

    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/{testId}/results")
    public String testResults(@PathVariable Long courseId,
                              @PathVariable Long chapterId,
                              @PathVariable Long testId,
                              Model model) {
        Test    test    = testService.getTestById(testId);
        Course  course  = getCourse(courseId);
        Chapter chapter = getChapter(chapterId);

        model.addAttribute("course",         course);
        model.addAttribute("chapter",        chapter);
        model.addAttribute("test",           test);
        model.addAttribute("attemptResults", testService.getAttemptResults(testId));
        model.addAttribute("questionCount",  testService.getQuestionsForTest(testId).size());
        return "tests/results";
    }

    // Edit test

    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/{testId}/edit")
    public String editTestPage(@PathVariable Long courseId,
                               @PathVariable Long chapterId,
                               @PathVariable Long testId,
                               Model model) {
        Test    test    = testService.getTestById(testId);
        String  raw     = test.getTestType();
        String  type    = raw != null && raw.contains(":") ? raw.split(":")[0] : raw;
        Integer timer   = parseTimer(raw);

        model.addAttribute("course",        getCourse(courseId));
        model.addAttribute("chapter",       getChapter(chapterId));
        model.addAttribute("test",          test);
        model.addAttribute("testType",      type);
        model.addAttribute("timerMinutes",  timer);
        model.addAttribute("questions",     testService.getQuestionsForTest(testId));
        return "tests/edit";
    }

    @PutMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/{testId}/edit")
    @ResponseBody
    public ResponseEntity<TestSummaryDTO> updateTest(@PathVariable Long courseId,
                                                     @PathVariable Long chapterId,
                                                     @PathVariable Long testId,
                                                     @RequestBody CreateTestRequest request,
                                                     Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return ResponseEntity.ok(testService.updateTest(courseId, chapterId, testId, email, request));
    }

    // Student: take exam

    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/{testId}/take")
    public String examPage(@PathVariable Long courseId,
                           @PathVariable Long chapterId,
                           @PathVariable Long testId,
                           Model model) {
        Test    test    = testService.getTestById(testId);
        String  raw     = test.getTestType();
        String  type    = raw != null && raw.contains(":") ? raw.split(":")[0] : raw;
        Integer timer   = parseTimer(raw);

        model.addAttribute("course",       getCourse(courseId));
        model.addAttribute("chapter",      getChapter(chapterId));
        model.addAttribute("test",         test);
        model.addAttribute("questions",    testService.getQuestionsForTest(testId));
        model.addAttribute("testType",     type);
        model.addAttribute("timerMinutes", timer);
        return "tests/exam";
    }

    // Student: submit

    @PostMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/{testId}/submit")
    @ResponseBody
    public ResponseEntity<TestResultDTO> submitTest(@PathVariable Long courseId,
                                                    @PathVariable Long chapterId,
                                                    @PathVariable Long testId,
                                                    @RequestBody Map<String, String> answers,
                                                    Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return ResponseEntity.ok(testService.submitTest(courseId, chapterId, testId, email, answers));
    }

    // Student: result page

    @GetMapping("/your-courses/{courseId}/chapters/{chapterId}/tests/{testId}/result/{attemptId}")
    public String resultPage(@PathVariable Long courseId,
                             @PathVariable Long chapterId,
                             @PathVariable Long testId,
                             @PathVariable Long attemptId,
                             Model model) {
        TestService.TestResultData data = testService.getResultPage(testId, attemptId);
        model.addAttribute("course",   getCourse(courseId));
        model.addAttribute("chapter",  getChapter(chapterId));
        model.addAttribute("test",     data.test());
        model.addAttribute("results",  data.results());
        model.addAttribute("score",    data.score());
        model.addAttribute("total",    data.total());
        model.addAttribute("pct",      data.pct());
        return "tests/result";
    }

    // helpers

    private Integer parseTimer(String raw) {
        if (raw != null && raw.contains(":")) {
            try { return Integer.parseInt(raw.split(":")[1]); } catch (Exception ignored) {}
        }
        return null;
    }

    private Course  getCourse(Long id)  { return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found")); }
    private Chapter getChapter(Long id) { return chapterRepository.findById(id).orElseThrow(() -> new RuntimeException("Chapter not found")); }
}