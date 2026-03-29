package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.*;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.TeacherDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherDashboardServiceImpl implements TeacherDashboardService {

    private final UserRepository                          userRepository;
    private final UserCourseRepository                    userCourseRepository;
    private final UserCourseEnrollmentRepository          enrollmentRepository;
    private final UserCourseTestRepository                uctRepository;
    private final UserCourseTestQuestionRepository        uctqRepository;
    private final UserCourseTestQuestionAttemptRepository uctqaRepository;
    private final AttemptRepository                       attemptRepository;

    @Override
    public TeacherDashboardDTO getDashboard(String teacherEmail) {
        User teacher = getUser(teacherEmail);

        // Get all courses owned by this teacher
        List<Course> courses = userCourseRepository.findByUser(teacher)
                .stream().map(UserCourse::getCourse).toList();

        if (courses.isEmpty()) {
            return new TeacherDashboardDTO(List.of(), 0, 0, false);
        }

        List<CourseProgressDTO> courseProgress = new ArrayList<>();
        int totalStudentsAll = 0;
        List<Integer> allAvgs = new ArrayList<>();

        for (Course course : courses) {
            // Get enrolled students
            List<User> students = enrollmentRepository.findByCourse(course)
                    .stream().map(uce -> uce.getUser())
                    .filter(u -> "STUDENT".equals(u.getRole()))
                    .toList();

            if (students.isEmpty()) {
                courseProgress.add(new CourseProgressDTO(
                        course.getCourseId(), course.getCourseName(), 0, 0, List.of()));
                continue;
            }

            List<StudentProgressDTO> studentDTOs = new ArrayList<>();

            for (User student : students) {
                // Get all attempts for this student in this course
                List<UserCourseTest> tests = uctRepository.findByCourse(course);
                int testsTaken    = 0;
                int totalCorrect  = 0;
                int totalQ        = 0;

                for (UserCourseTest uct : tests) {
                    Set<Long> attemptIds = uctqaRepository
                            .findByTestAndUser(uct.getTest(), student)
                            .stream().map(UserCourseTestQuestionAttempt::getAttemptId)
                            .collect(Collectors.toSet());

                    if (attemptIds.isEmpty()) continue;

                    long qCount = uctqRepository.findByTest(uct.getTest()).size();
                    testsTaken++;
                    totalQ += (int) qCount;

                    // Take the best attempt score
                    int bestScore = attemptIds.stream()
                            .map(aid -> attemptRepository.findById(aid).orElse(null))
                            .filter(Objects::nonNull)
                            .mapToInt(a -> a.getScore() != null ? a.getScore() : 0)
                            .max().orElse(0);
                    totalCorrect += bestScore;
                }

                int avgPct = totalQ > 0 ? (int) ((totalCorrect * 100.0) / totalQ) : 0;
                studentDTOs.add(new StudentProgressDTO(
                        student.getUserId(),
                        student.getUsername(),
                        student.getEmail(),
                        testsTaken,
                        avgPct,
                        totalCorrect,
                        totalQ
                ));
            }

            // Sort students by avg descending
            studentDTOs.sort(Comparator.comparingInt(StudentProgressDTO::avgPct).reversed());

            int classAvg = studentDTOs.isEmpty() ? 0
                    : (int) studentDTOs.stream().mapToInt(StudentProgressDTO::avgPct).average().orElse(0);

            courseProgress.add(new CourseProgressDTO(
                    course.getCourseId(), course.getCourseName(),
                    students.size(), classAvg, studentDTOs));

            totalStudentsAll += students.size();
            if (classAvg > 0) allAvgs.add(classAvg);
        }

        int overallAvg = allAvgs.isEmpty() ? 0
                : (int) allAvgs.stream().mapToInt(Integer::intValue).average().orElse(0);

        return new TeacherDashboardDTO(courseProgress, totalStudentsAll, overallAvg, true);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}