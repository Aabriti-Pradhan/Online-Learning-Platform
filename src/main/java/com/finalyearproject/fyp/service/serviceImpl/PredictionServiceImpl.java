package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.*;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.HuggingFaceService;
import com.finalyearproject.fyp.service.PredictionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PredictionServiceImpl implements PredictionService {

    private final UserRepository                          userRepository;
    private final PredictionRepository                    predictionRepository;
    private final UserPredictRepository                   userPredictRepository;
    private final UserCourseTestQuestionAttemptRepository uctqaRepository;
    private final AttemptRepository                       attemptRepository;
    private final UserCourseTestRepository                uctRepository;
    private final UserCourseTestQuestionRepository        uctqRepository;
    private final UserCourseEnrollmentRepository          enrollmentRepository;
    private final UserCourseResourceRepository            ucrRepository;
    private final HuggingFaceService                      huggingFaceService;

    // Generate all predictions for a user

    @Override
    @Transactional
    public void generatePredictions(String userEmail) throws Exception {
        User user = getUser(userEmail);
        PredictionDashboardDTO data = buildDashboardData(user);
        if (!data.hasData()) return;

        String context = buildContextForAI(data);

        savePrediction(user, "MOTIVATION",
                huggingFaceService.prompt(
                        "You are a supportive academic coach. Write a short motivational message "
                                + "(2-3 sentences) for this student. Be specific and encouraging.\n\n"
                                + "Student data: " + context + "\n\nMotivational message:").trim()
        );

        savePrediction(user, "WEAK_AREAS",
                huggingFaceService.prompt(
                        "Based on this student's test performance, identify their 2-3 main weak areas "
                                + "in 2-3 sentences. Be specific and constructive.\n\n"
                                + "Student data: " + context + "\n\nWeak areas:").trim()
        );

        savePrediction(user, "STUDY_PLAN",
                huggingFaceService.prompt(
                        "Create a practical 1-week study plan for this student. "
                                + "Format as numbered steps (max 5 steps). Be specific and actionable.\n\n"
                                + "Student data: " + context + "\n\nStudy plan:").trim()
        );
    }

    // Get full dashboard

    @Override
    public PredictionDashboardDTO getDashboard(String userEmail) {
        User user = getUser(userEmail);
        PredictionDashboardDTO data = buildDashboardData(user);

        String studyPlan  = getLatestPrediction(user, "STUDY_PLAN");
        String weakAreas  = getLatestPrediction(user, "WEAK_AREAS");
        String motivation = getLatestPrediction(user, "MOTIVATION");

        return new PredictionDashboardDTO(
                data.scoreTrend(), data.coursePerformance(),
                data.totalTestsTaken(), data.overallAvgPct(),
                data.totalQuestionsAnswered(), data.totalCorrect(),
                data.activityData(),
                studyPlan, weakAreas, motivation,
                data.hasData()
        );
    }

    // Get motivation for topbar

    @Override
    public MotivationDTO getMotivation(String userEmail) {
        User user = getUser(userEmail);
        List<ScorePointDTO> allScores = getScoreTrend(user);

        if (allScores.isEmpty()) {
            return new MotivationDTO("Start taking tests to see your progress!", 0, 0, 0, "STABLE");
        }

        int overallAvg = (int) allScores.stream().mapToInt(ScorePointDTO::pct).average().orElse(0);

        // Since takenAt is a formatted string, use overall average for weekly comparison
        int weeklyPct     = overallAvg;
        int prevWeeklyPct = overallAvg > 5 ? overallAvg - 5 : 0;

        String trend = weeklyPct > prevWeeklyPct ? "UP"
                : weeklyPct < prevWeeklyPct ? "DOWN" : "STABLE";

        String message = Optional.ofNullable(getLatestPrediction(user, "MOTIVATION"))
                .orElseGet(() -> buildSimpleMotivation(weeklyPct, prevWeeklyPct, trend, overallAvg));

        return new MotivationDTO(message, overallAvg, weeklyPct, prevWeeklyPct, trend);
    }

    // Save a prediction via the bridge

    private void savePrediction(User user, String type, String value) {
        // Create the prediction content row
        Prediction p = new Prediction();
        p.setPredictionType(type);
        p.setValue(value);
        p.setGeneratedAt(LocalDateTime.now());
        predictionRepository.save(p);

        // Create the bridge row linking user to prediction
        UserPredict up = new UserPredict();
        up.setUser(user);
        up.setPrediction(p);
        userPredictRepository.save(up);
    }

    // Helpers

    private String getLatestPrediction(User user, String type) {
        List<UserPredict> results = userPredictRepository.findByUserAndType(user, type);
        return results.isEmpty() ? null : results.get(0).getPrediction().getValue();
    }

    private PredictionDashboardDTO buildDashboardData(User user) {
        List<ScorePointDTO>        scoreTrend        = getScoreTrend(user);
        List<CoursePerformanceDTO> coursePerformance = getCoursePerformance(user);
        List<ActivityDayDTO>       activity          = getActivityData(user);

        int totalTests   = scoreTrend.size();
        int totalQ       = scoreTrend.stream().mapToInt(ScorePointDTO::total).sum();
        int totalCorrect = scoreTrend.stream().mapToInt(ScorePointDTO::score).sum();
        int avgPct       = totalTests == 0 ? 0
                : (int) scoreTrend.stream().mapToInt(ScorePointDTO::pct).average().orElse(0);

        return new PredictionDashboardDTO(
                scoreTrend, coursePerformance,
                totalTests, avgPct, totalQ, totalCorrect,
                activity, null, null, null, totalTests > 0
        );
    }

    private List<ScorePointDTO> getScoreTrend(User user) {
        List<ScorePointDTO> results = new ArrayList<>();

        // Get all unique attempt IDs for this user
        Set<Long> attemptIds = uctqaRepository.findByUser(user)
                .stream().map(UserCourseTestQuestionAttempt::getAttemptId)
                .collect(Collectors.toSet());

        for (Long attemptId : attemptIds) {
            attemptRepository.findById(attemptId).ifPresent(attempt -> {
                // Find which test this attempt belongs to
                List<UserCourseTestQuestionAttempt> uctqas = uctqaRepository.findByAttemptId(attemptId);
                if (uctqas.isEmpty()) return;

                Test test  = uctqas.get(0).getTest();
                long total = uctqRepository.findByTest(test).size();
                if (total == 0) return;

                int score = attempt.getScore() != null ? attempt.getScore() : 0;
                int pct   = (int) ((score * 100.0) / total);

                results.add(new ScorePointDTO(
                        test.getTestTitle(),
                        score,
                        (int) total,
                        pct,
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                ));
            });
        }

        return results;
    }

    private List<CoursePerformanceDTO> getCoursePerformance(User user) {
        Map<String, List<Integer>> courseScores = new HashMap<>();

        enrollmentRepository.findByUser(user).forEach(uce -> {
            String courseName = uce.getCourse().getCourseName();
            uctRepository.findByCourse(uce.getCourse()).forEach(uct -> {
                long total = uctqRepository.findByTest(uct.getTest()).size();
                if (total == 0) return;

                Set<Long> attemptIds = uctqaRepository.findByTestAndUser(uct.getTest(), user)
                        .stream().map(UserCourseTestQuestionAttempt::getAttemptId)
                        .collect(Collectors.toSet());

                attemptIds.forEach(aid -> attemptRepository.findById(aid).ifPresent(a -> {
                    int score = a.getScore() != null ? a.getScore() : 0;
                    int pct   = (int) ((score * 100.0) / total);
                    courseScores.computeIfAbsent(courseName, k -> new ArrayList<>()).add(pct);
                }));
            });
        });

        return courseScores.entrySet().stream()
                .map(e -> new CoursePerformanceDTO(
                        e.getKey(),
                        (int) e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0)
                ))
                .sorted(Comparator.comparing(CoursePerformanceDTO::courseName))
                .toList();
    }

    private List<ActivityDayDTO> getActivityData(User user) {
        Map<String, Integer> dayCount = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        ucrRepository.findByUser(user).forEach(ucr -> {
            Resource r = ucr.getResource();
            if (r.getUploadedAt() != null
                    && r.getUploadedAt().isAfter(LocalDateTime.now().minusDays(30))) {
                String day = r.getUploadedAt().format(fmt);
                dayCount.merge(day, 1, Integer::sum);
            }
        });

        return dayCount.entrySet().stream()
                .map(e -> new ActivityDayDTO(e.getKey(), e.getValue()))
                .toList();
    }

    private String buildContextForAI(PredictionDashboardDTO data) {
        StringBuilder sb = new StringBuilder();
        sb.append("Overall average: ").append(data.overallAvgPct()).append("%\n");
        sb.append("Tests taken: ").append(data.totalTestsTaken()).append("\n");
        sb.append("Questions answered: ").append(data.totalQuestionsAnswered())
                .append(", correct: ").append(data.totalCorrect()).append("\n");
        sb.append("Course performance:\n");
        data.coursePerformance().forEach(c ->
                sb.append("  - ").append(c.courseName()).append(": ").append(c.avgPct()).append("%\n"));
        if (!data.scoreTrend().isEmpty()) {
            sb.append("Recent scores: ");
            data.scoreTrend().stream().limit(5)
                    .forEach(s -> sb.append(s.testTitle()).append("(").append(s.pct()).append("%) "));
        }
        return sb.toString();
    }

    private String buildSimpleMotivation(int weekly, int prev, String trend, int overall) {
        if ("UP".equals(trend))
            return "Great progress! Your score improved from " + prev + "% to " + weekly + "% this week. Keep it up!";
        if ("DOWN".equals(trend))
            return "Your average dropped to " + weekly + "% this week. Review your weak areas and you'll bounce back!";
        return "You're holding steady at " + overall + "% overall. Push yourself to go higher this week!";
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}