package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.*;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.TestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final CourseRepository                        courseRepository;
    private final ChapterRepository                       chapterRepository;
    private final UserRepository                          userRepository;
    private final TestRepository                          testRepository;
    private final QuestionRepository                      questionRepository;
    private final AttemptRepository                       attemptRepository;
    private final AttemptAnsRepository                    attemptAnsRepository;
    private final UserCourseTestRepository                uctRepository;
    private final UserCourseTestQuestionRepository        uctqRepository;
    private final UserCourseTestQuestionAttemptRepository uctqaRepository;

    @Override
    public List<TestSummaryDTO> getTestsForTeacher(Long chapterId) {
        Chapter chapter = getChapter(chapterId);
        return uctRepository.findByChapter(chapter).stream().map(uct -> {
            Test t      = uct.getTest();
            long qCount = uctqRepository.findByTest(t).size();
            long aCount = uctqaRepository.findByTest(t).stream()
                    .map(UserCourseTestQuestionAttempt::getAttemptId).distinct().count();
            return new TestSummaryDTO(t.getTestId(), t.getTestTitle(),
                    parseType(t.getTestType()), t.getCreatedAt(), qCount, aCount);
        }).toList();
    }

    @Override
    public List<StudentTestDTO> getTestsForStudent(Long chapterId, String userEmail) {
        Chapter chapter = getChapter(chapterId);
        User    user    = getUser(userEmail);

        return uctRepository.findByChapter(chapter).stream().map(uct -> {
            Test t      = uct.getTest();
            long qCount = uctqRepository.findByTest(t).size();

            Set<Long> myAttemptIds = uctqaRepository.findByTestAndUser(t, user)
                    .stream().map(UserCourseTestQuestionAttempt::getAttemptId).collect(Collectors.toSet());

            List<AttemptDTO> past = myAttemptIds.stream().map(aid -> {
                Attempt a = attemptRepository.findById(aid).orElse(null);
                return new AttemptDTO(aid, a != null ? a.getScore() : 0, qCount);
            }).toList();

            return new StudentTestDTO(t.getTestId(), t.getTestTitle(),
                    parseType(t.getTestType()), parseTimer(t.getTestType()), qCount, past);
        }).toList();
    }

    @Override
    @Transactional
    public TestSummaryDTO createTest(Long courseId, Long chapterId, String userEmail, CreateTestRequest req) {
        Course  course  = getCourse(courseId);
        Chapter chapter = getChapter(chapterId);
        User    user    = getUser(userEmail);

        Test test = new Test();
        test.setTestTitle(req.testTitle());
        test.setTestType(buildTestType(req.testType(), req.timerMinutes()));
        test.setCreatedAt(LocalDateTime.now());
        testRepository.save(test);

        UserCourseTest uct = new UserCourseTest();
        uct.setTestId(test.getTestId());
        uct.setCourseId(course.getCourseId());
        uct.setUserId(user.getUserId());
        uct.setChapter(chapter);
        uctRepository.save(uct);

        saveQuestions(req.questions(), test, course, user);

        return new TestSummaryDTO(test.getTestId(), test.getTestTitle(),
                req.testType(), test.getCreatedAt(), req.questions().size(), 0);
    }

    @Override
    @Transactional
    public TestSummaryDTO updateTest(Long courseId, Long chapterId, Long testId,
                                     String userEmail, CreateTestRequest req) {
        Test   test   = getTest(testId);
        Course course = getCourse(courseId);
        User   user   = getUser(userEmail);

        test.setTestTitle(req.testTitle());
        test.setTestType(buildTestType(req.testType(), req.timerMinutes()));
        testRepository.save(test);

        uctqRepository.deleteAll(uctqRepository.findByTest(test));
        saveQuestions(req.questions(), test, course, user);

        return new TestSummaryDTO(test.getTestId(), test.getTestTitle(),
                req.testType(), test.getCreatedAt(), req.questions().size(), 0);
    }

    @Override
    @Transactional
    public void deleteTest(Long courseId, Long testId) {
        Test test = getTest(testId);
        uctqaRepository.deleteAll(uctqaRepository.findByTest(test));
        uctqRepository.deleteAll(uctqRepository.findByTest(test));
        uctRepository.deleteAll(uctRepository.findByCourse(getCourse(courseId))
                .stream().filter(u -> u.getTestId().equals(testId)).toList());
        testRepository.delete(test);
    }

    @Override
    public List<Question> getQuestionsForTest(Long testId) {
        return uctqRepository.findByTest(getTest(testId))
                .stream().map(UserCourseTestQuestion::getQuestion).toList();
    }

    @Override
    public Test getTestById(Long testId) {
        return getTest(testId);
    }

    @Override
    @Transactional
    public TestResultDTO submitTest(Long courseId, Long chapterId, Long testId,
                                    String userEmail, Map<String, String> answers) {
        Test   test   = getTest(testId);
        Course course = getCourse(courseId);
        User   user   = getUser(userEmail);

        List<UserCourseTestQuestion> uctqs = uctqRepository.findByTest(test);
        int correct = 0;
        for (UserCourseTestQuestion uctq : uctqs) {
            Question q = uctq.getQuestion();
            if (q.getCorrectAns() != null &&
                    q.getCorrectAns().equals(answers.get(String.valueOf(q.getQuestionId())))) correct++;
        }

        Attempt attempt = new Attempt();
        attempt.setScore(correct);
        attemptRepository.save(attempt);

        for (UserCourseTestQuestion uctq : uctqs) {
            Question q        = uctq.getQuestion();
            String   selected = answers.getOrDefault(String.valueOf(q.getQuestionId()), "");

            AttemptAns ans = new AttemptAns();
            ans.setAttemptId(attempt.getAttemptId());
            ans.setQuestionId(q.getQuestionId());
            ans.setSelectedAns(selected);
            attemptAnsRepository.save(ans);

            UserCourseTestQuestionAttempt ucta = new UserCourseTestQuestionAttempt();
            ucta.setAttemptId(attempt.getAttemptId());
            ucta.setQuestionId(q.getQuestionId());
            ucta.setTestId(test.getTestId());
            ucta.setCourseId(course.getCourseId());
            ucta.setUserId(user.getUserId());
            uctqaRepository.save(ucta);
        }

        return new TestResultDTO(attempt.getAttemptId(), correct, uctqs.size());
    }

    @Override
    public TestResultData getResultPage(Long testId, Long attemptId) {
        Test    test    = getTest(testId);
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        Map<Long, String> selectedMap = attemptAnsRepository.findByAttemptId(attemptId)
                .stream().collect(Collectors.toMap(AttemptAns::getQuestionId, AttemptAns::getSelectedAns));

        List<QuestionResult> results = uctqRepository.findByTest(test).stream().map(uctq -> {
            Question q       = uctq.getQuestion();
            String   sel     = selectedMap.getOrDefault(q.getQuestionId(), "");
            boolean  correct = q.getCorrectAns().equals(sel);
            return new QuestionResult(q, sel, correct);
        }).toList();

        int total = results.size();
        int score = attempt.getScore();
        int pct   = total > 0 ? score * 100 / total : 0;

        return new TestResultData(test, results, score, total, pct);
    }

    @Override
    public List<AttemptResultDTO> getAttemptResults(Long testId) {
        Test test = getTest(testId);
        List<UserCourseTestQuestion> uctqs = uctqRepository.findByTest(test);
        int total = uctqs.size();

        List<UserCourseTestQuestionAttempt> all = uctqaRepository.findByTest(test);
        Map<Long, AttemptResultDTO> byAttempt = new LinkedHashMap<>();

        for (UserCourseTestQuestionAttempt a : all) {
            byAttempt.computeIfAbsent(a.getAttemptId(), id -> new AttemptResultDTO(
                    id,
                    a.getUser().getUsername(),
                    a.getUser().getEmail(),
                    a.getAttempt().getScore(),
                    total
            ));
        }
        return new ArrayList<>(byAttempt.values());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void saveQuestions(List<QuestionDTO> dtos, Test test, Course course, User user) {
        if (dtos == null) return;
        for (QuestionDTO dto : dtos) {
            Question q = dto.questionId() != null
                    ? questionRepository.findById(dto.questionId()).orElse(new Question())
                    : new Question();
            q.setQuestionText(dto.questionText());
            q.setOptionA(dto.optionA()); q.setOptionB(dto.optionB());
            q.setOptionC(dto.optionC()); q.setOptionD(dto.optionD());
            q.setCorrectAns(dto.correctAns());
            questionRepository.save(q);

            UserCourseTestQuestion uctq = new UserCourseTestQuestion();
            uctq.setQuestionId(q.getQuestionId());
            uctq.setTestId(test.getTestId());
            uctq.setCourseId(course.getCourseId());
            uctq.setUserId(user.getUserId());
            uctqRepository.save(uctq);
        }
    }

    private String buildTestType(String type, Integer timer) {
        return "PRACTICE_EXAM".equals(type) && timer != null ? "PRACTICE_EXAM:" + timer : type;
    }

    private String parseType(String raw) {
        return raw != null && raw.contains(":") ? raw.split(":")[0] : raw;
    }

    private Integer parseTimer(String raw) {
        if (raw != null && raw.contains(":")) {
            try { return Integer.parseInt(raw.split(":")[1]); } catch (Exception ignored) {}
        }
        return null;
    }

    private Course  getCourse(Long id)  { return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found: " + id)); }
    private Chapter getChapter(Long id) { return chapterRepository.findById(id).orElseThrow(() -> new RuntimeException("Chapter not found: " + id)); }
    private Test    getTest(Long id)    { return testRepository.findById(id).orElseThrow(() -> new RuntimeException("Test not found: " + id)); }
    private User    getUser(String e)   { return userRepository.findByEmail(e).orElseThrow(() -> new RuntimeException("User not found: " + e)); }
}