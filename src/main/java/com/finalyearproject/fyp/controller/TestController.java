package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class TestController {

    private final CourseRepository                        courseRepository;
    private final UserRepository                          userRepository;
    private final TestRepository                          testRepository;
    private final QuestionRepository                      questionRepository;
    private final AttemptRepository                       attemptRepository;
    private final AttemptAnsRepository                    attemptAnsRepository;
    private final UserCourseTestRepository                uctRepository;
    private final UserCourseTestQuestionRepository        uctqRepository;
    private final UserCourseTestQuestionAttemptRepository uctqaRepository;

    // ── Teacher/Student: test dashboard ──────────────────────────────────────

    @GetMapping("/your-courses/{courseId}/tests")
    public String testsDashboard(@PathVariable Long courseId, Model model, Authentication auth) {
        Course course = getCourse(courseId);
        model.addAttribute("course", course);

        boolean isStudent = auth.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));
        boolean isTeacher = auth.getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER"));

        model.addAttribute("isTeacher", isTeacher);

        if (isStudent) {
            // Students: show available tests with their past attempts
            User user = resolveUser(auth);
            List<Map<String, Object>> testData = uctRepository.findByCourse(course).stream().map(uct -> {
                Test t      = uct.getTest();
                long qCount = uctqRepository.findByTest(t).size();

                Set<Long> myAttemptIds = uctqaRepository.findByTestAndUser(t, user)
                        .stream().map(UserCourseTestQuestionAttempt::getAttemptId).collect(Collectors.toSet());

                List<Map<String, Object>> past = myAttemptIds.stream().map(aid -> {
                    Attempt a = attemptRepository.findById(aid).orElse(null);
                    return Map.<String, Object>of("attemptId", aid, "score", a != null ? a.getScore() : 0, "total", qCount);
                }).toList();

                String rawType = t.getTestType();
                String type    = rawType != null && rawType.contains(":") ? rawType.split(":")[0] : rawType;
                Integer timer  = null;
                if (rawType != null && rawType.contains(":")) {
                    try { timer = Integer.parseInt(rawType.split(":")[1]); } catch (Exception ignored) {}
                }
                Map<String, Object> m = new HashMap<>();
                m.put("testId", t.getTestId()); m.put("testTitle", t.getTestTitle());
                m.put("testType", type); m.put("timerMinutes", timer);
                m.put("questionCount", qCount); m.put("pastAttempts", past);
                return m;
            }).toList();
            model.addAttribute("testData", testData);
            return "tests/take";
        }

        // Teachers: show all tests with stats
        List<Map<String, Object>> summaries = uctRepository.findByCourse(course).stream().map(uct -> {
            Test t      = uct.getTest();
            long qCount = uctqRepository.findByTest(t).size();
            long aCount = uctqaRepository.findByTest(t).stream()
                    .map(a -> a.getAttemptId()).distinct().count();
            String rawType = t.getTestType();
            String type    = rawType != null && rawType.contains(":") ? rawType.split(":")[0] : rawType;
            Map<String, Object> m = new HashMap<>();
            m.put("testId",        t.getTestId());
            m.put("testTitle",     t.getTestTitle());
            m.put("testType",      type);
            m.put("createdAt",     t.getCreatedAt());
            m.put("questionCount", qCount);
            m.put("attemptCount",  aCount);
            return m;
        }).toList();
        model.addAttribute("testSummaries", summaries);
        return "tests/index";
    }

    // ── Teacher: create test page ─────────────────────────────────────────────

    @GetMapping("/your-courses/{courseId}/tests/create")
    public String createTestPage(@PathVariable Long courseId, Model model) {
        model.addAttribute("course", getCourse(courseId));
        return "tests/create";
    }

    // ── Teacher: save test + questions ────────────────────────────────────────

    @PostMapping("/your-courses/{courseId}/tests/create")
    @ResponseBody
    public ResponseEntity<?> saveTest(@PathVariable Long courseId,
                                      @RequestBody Map<String, Object> payload,
                                      Authentication auth) {
        Course course = getCourse(courseId);
        User   user   = resolveUser(auth);

        String  title     = (String) payload.get("testTitle");
        String  testType  = (String) payload.get("testType");
        Integer timer     = payload.get("timerMinutes") != null
                ? Integer.valueOf(payload.get("timerMinutes").toString()) : null;

        Test test = new Test();
        test.setTestTitle(title);
        test.setTestType("PRACTICE_EXAM".equals(testType) && timer != null
                ? "PRACTICE_EXAM:" + timer : testType);
        test.setCreatedAt(LocalDateTime.now());
        testRepository.save(test);

        UserCourseTest uct = new UserCourseTest();
        uct.setTestId(test.getTestId()); uct.setCourseId(course.getCourseId()); uct.setUserId(user.getUserId());
        uctRepository.save(uct);

        List<Map<String, String>> qs = (List<Map<String, String>>) payload.get("questions");
        for (Map<String, String> q : qs) {
            Question question = new Question();
            question.setQuestionText(q.get("questionText"));
            question.setOptionA(q.get("optionA")); question.setOptionB(q.get("optionB"));
            question.setOptionC(q.get("optionC")); question.setOptionD(q.get("optionD"));
            question.setCorrectAns(q.get("correctAns"));
            questionRepository.save(question);

            UserCourseTestQuestion uctq = new UserCourseTestQuestion();
            uctq.setQuestionId(question.getQuestionId()); uctq.setTestId(test.getTestId());
            uctq.setCourseId(course.getCourseId()); uctq.setUserId(user.getUserId());
            uctqRepository.save(uctq);
        }

        return ResponseEntity.ok(Map.of("testId", test.getTestId(), "testTitle", test.getTestTitle()));
    }

    // ── Teacher: delete test ──────────────────────────────────────────────────

    @DeleteMapping("/your-courses/{courseId}/tests/{testId}")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> deleteTest(@PathVariable Long courseId, @PathVariable Long testId) {
        Test test = getTest(testId);
        uctqaRepository.deleteAll(uctqaRepository.findByTest(test));
        uctqRepository.deleteAll(uctqRepository.findByTest(test));
        uctRepository.deleteAll(uctRepository.findByCourse(getCourse(courseId))
                .stream().filter(u -> u.getTestId().equals(testId)).toList());
        testRepository.delete(test);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }

    // ── Teacher: per-student results ──────────────────────────────────────────

    @GetMapping("/your-courses/{courseId}/tests/{testId}/results")
    public String testResults(@PathVariable Long courseId, @PathVariable Long testId, Model model) {
        Test   test   = getTest(testId);
        Course course = getCourse(courseId);
        model.addAttribute("test", test); model.addAttribute("course", course);

        List<UserCourseTestQuestionAttempt> all = uctqaRepository.findByTest(test);
        Map<Long, Map<String, Object>> byAttempt = new LinkedHashMap<>();
        for (UserCourseTestQuestionAttempt a : all) {
            byAttempt.computeIfAbsent(a.getAttemptId(), id -> {
                Map<String, Object> e = new HashMap<>();
                e.put("studentName",  a.getUser().getUsername());
                e.put("studentEmail", a.getUser().getEmail());
                e.put("score",        a.getAttempt().getScore());
                e.put("total",        uctqRepository.findByTest(test).size());
                e.put("attemptId",    id);
                return e;
            });
        }
        model.addAttribute("attemptResults", new ArrayList<>(byAttempt.values()));
        model.addAttribute("questionCount",  uctqRepository.findByTest(test).size());
        return "tests/results";
    }

    // ── Teacher: edit test page ───────────────────────────────────────────────

    @GetMapping("/your-courses/{courseId}/tests/{testId}/edit")
    public String editTestPage(@PathVariable Long courseId,
                               @PathVariable Long testId,
                               Model model) {
        Test   test   = getTest(testId);
        Course course = getCourse(courseId);

        String rawType = test.getTestType();
        String type    = rawType != null && rawType.contains(":") ? rawType.split(":")[0] : rawType;
        Integer timer  = null;
        if (rawType != null && rawType.contains(":")) {
            try { timer = Integer.parseInt(rawType.split(":")[1]); } catch (Exception ignored) {}
        }

        List<Question> questions = uctqRepository.findByTest(test)
                .stream().map(UserCourseTestQuestion::getQuestion).toList();

        model.addAttribute("course",        course);
        model.addAttribute("test",          test);
        model.addAttribute("testType",      type);
        model.addAttribute("timerMinutes",  timer);
        model.addAttribute("questions",     questions);
        return "tests/edit";
    }

    // ── Teacher: save edits to existing test ─────────────────────────────────

    @PutMapping("/your-courses/{courseId}/tests/{testId}/edit")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> updateTest(@PathVariable Long courseId,
                                        @PathVariable Long testId,
                                        @RequestBody Map<String, Object> payload,
                                        Authentication auth) {
        Test   test   = getTest(testId);
        Course course = getCourse(courseId);
        User   user   = resolveUser(auth);

        String  title    = (String) payload.get("testTitle");
        String  testType = (String) payload.get("testType");
        Integer timer    = payload.get("timerMinutes") != null
                ? Integer.valueOf(payload.get("timerMinutes").toString()) : null;

        test.setTestTitle(title);
        test.setTestType("PRACTICE_EXAM".equals(testType) && timer != null
                ? "PRACTICE_EXAM:" + timer : testType);
        testRepository.save(test);

        // Delete old questions and re-save new ones
        List<UserCourseTestQuestion> oldUctqs = uctqRepository.findByTest(test);
        uctqRepository.deleteAll(oldUctqs);

        List<Map<String, Object>> qs = (List<Map<String, Object>>) payload.get("questions");
        for (Map<String, Object> q : qs) {
            Long qId = q.get("questionId") != null
                    ? Long.valueOf(q.get("questionId").toString()) : null;

            Question question = qId != null
                    ? questionRepository.findById(qId).orElse(new Question())
                    : new Question();

            question.setQuestionText((String) q.get("questionText"));
            question.setOptionA((String) q.get("optionA"));
            question.setOptionB((String) q.get("optionB"));
            question.setOptionC((String) q.get("optionC"));
            question.setOptionD((String) q.get("optionD"));
            question.setCorrectAns((String) q.get("correctAns"));
            questionRepository.save(question);

            UserCourseTestQuestion uctq = new UserCourseTestQuestion();
            uctq.setQuestionId(question.getQuestionId());
            uctq.setTestId(test.getTestId());
            uctq.setCourseId(course.getCourseId());
            uctq.setUserId(user.getUserId());
            uctqRepository.save(uctq);
        }

        return ResponseEntity.ok(Map.of("testId", test.getTestId(), "testTitle", test.getTestTitle()));
    }

    // ── Student: available tests list (kept for direct URL access) ────────────

    @GetMapping("/your-courses/{courseId}/tests/take")
    public String takeTestList(@PathVariable Long courseId, Model model, Authentication auth) {
        // Delegate to the main tests endpoint which handles role-based routing
        return testsDashboard(courseId, model, auth);
    }

    // ── Student: exam page ────────────────────────────────────────────────────

    @GetMapping("/your-courses/{courseId}/tests/{testId}/take")
    public String examPage(@PathVariable Long courseId, @PathVariable Long testId, Model model) {
        Test   test   = getTest(testId);
        Course course = getCourse(courseId);

        String rawType = test.getTestType();
        String type    = rawType != null && rawType.contains(":") ? rawType.split(":")[0] : rawType;
        Integer timer  = null;
        if (rawType != null && rawType.contains(":")) {
            try { timer = Integer.parseInt(rawType.split(":")[1]); } catch (Exception ignored) {}
        }

        List<Question> questions = uctqRepository.findByTest(test)
                .stream().map(UserCourseTestQuestion::getQuestion).toList();

        model.addAttribute("test", test); model.addAttribute("course", course);
        model.addAttribute("questions", questions);
        model.addAttribute("testType", type); model.addAttribute("timerMinutes", timer);
        return "tests/exam";
    }

    // ── Student: submit answers ───────────────────────────────────────────────

    @PostMapping("/your-courses/{courseId}/tests/{testId}/submit")
    @ResponseBody
    public ResponseEntity<?> submitTest(@PathVariable Long courseId,
                                        @PathVariable Long testId,
                                        @RequestBody Map<String, String> answers,
                                        Authentication auth) {
        Test   test   = getTest(testId);
        Course course = getCourse(courseId);
        User   user   = resolveUser(auth);

        List<UserCourseTestQuestion> uctqs = uctqRepository.findByTest(test);
        int correct = 0;
        for (UserCourseTestQuestion uctq : uctqs) {
            Question q = uctq.getQuestion();
            if (q.getCorrectAns() != null && q.getCorrectAns().equals(
                    answers.get(String.valueOf(q.getQuestionId())))) correct++;
        }

        Attempt attempt = new Attempt();
        attempt.setScore(correct);
        attemptRepository.save(attempt);

        for (UserCourseTestQuestion uctq : uctqs) {
            Question q        = uctq.getQuestion();
            String   selected = answers.getOrDefault(String.valueOf(q.getQuestionId()), "");

            AttemptAns ans = new AttemptAns();
            ans.setAttemptId(attempt.getAttemptId()); ans.setQuestionId(q.getQuestionId());
            ans.setSelectedAns(selected);
            attemptAnsRepository.save(ans);

            UserCourseTestQuestionAttempt ucta = new UserCourseTestQuestionAttempt();
            ucta.setAttemptId(attempt.getAttemptId()); ucta.setQuestionId(q.getQuestionId());
            ucta.setTestId(test.getTestId()); ucta.setCourseId(course.getCourseId());
            ucta.setUserId(user.getUserId());
            uctqaRepository.save(ucta);
        }

        return ResponseEntity.ok(Map.of(
                "attemptId", attempt.getAttemptId(),
                "score",     correct,
                "total",     uctqs.size()
        ));
    }

    // ── Student: result page ──────────────────────────────────────────────────

    @GetMapping("/your-courses/{courseId}/tests/{testId}/result/{attemptId}")
    public String resultPage(@PathVariable Long courseId, @PathVariable Long testId,
                             @PathVariable Long attemptId, Model model) {
        Test    test    = getTest(testId);
        Course  course  = getCourse(courseId);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        List<UserCourseTestQuestion> uctqs = uctqRepository.findByTest(test);
        Map<Long, String> selectedMap = attemptAnsRepository.findByAttemptId(attemptId)
                .stream().collect(Collectors.toMap(AttemptAns::getQuestionId, AttemptAns::getSelectedAns));

        List<Map<String, Object>> results = uctqs.stream().map(uctq -> {
            Question q       = uctq.getQuestion();
            String   sel     = selectedMap.getOrDefault(q.getQuestionId(), "");
            boolean  correct = q.getCorrectAns().equals(sel);
            Map<String, Object> r = new HashMap<>();
            r.put("question", q); r.put("selected", sel); r.put("correct", correct);
            return r;
        }).toList();

        int total = uctqs.size();
        int score = attempt.getScore();
        model.addAttribute("test", test); model.addAttribute("course", course);
        model.addAttribute("attempt", attempt); model.addAttribute("results", results);
        model.addAttribute("total", total); model.addAttribute("score", score);
        model.addAttribute("pct", total > 0 ? score * 100 / total : 0);
        return "tests/result";
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Course getCourse(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found"));
    }
    private Test getTest(Long id) {
        return testRepository.findById(id).orElseThrow(() -> new RuntimeException("Test not found"));
    }
    private User resolveUser(Authentication auth) {
        return userRepository.findByEmail(YourCoursesController.extractEmail(auth))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}