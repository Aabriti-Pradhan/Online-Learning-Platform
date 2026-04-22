package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.EnrollmentDTO;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.EnrollmentService;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final CourseRepository                       courseRepository;
    private final UserRepository                         userRepository;
    private final EnrollmentRepository                   enrollmentRepository;
    private final UserCourseEnrollmentRepository         uceRepository;
    private final UserCourseRepository                   userCourseRepository;
    private final UserCourseTestQuestionAttemptRepository uctqaRepository;
    private final AttemptRepository                      attemptRepository;
    private final UserCourseTestQuestionRepository       uctqRepository;
    private final UserCourseTestRepository               uctRepository;
    private final UserCourseResourceTagRepository        ucrtRepository;
    private final UserCourseResourceRepository           ucrRepository;
    private final NotificationService                    notificationService;

    @Override
    @Transactional
    public EnrollmentDTO enroll(String userEmail, Long courseId) {
        User   user   = getUser(userEmail);
        Course course = getCourse(courseId);

        if (uceRepository.findByUserAndCourse(user, course).isPresent()) {
            throw new IllegalStateException("Already enrolled in this course");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus("ACTIVE");
        enrollmentRepository.save(enrollment);

        UserCourseEnrollment uce = new UserCourseEnrollment();
        uce.setEnrollmentId(enrollment.getEnrollmentId());
        uce.setCourseId(course.getCourseId());
        uce.setUserId(user.getUserId());
        uceRepository.save(uce);

        // Notify the course teacher(s)
        List<Long> teacherIds = userCourseRepository.findByCourse(course)
                .stream().map(uc -> uc.getUser().getUserId()).toList();
        notificationService.sendToUsers(
                teacherIds,
                "New Enrollment",
                user.getUsername() + " enrolled in " + course.getCourseName(),
                "ENROLLMENT",
                courseId
        );

        return new EnrollmentDTO("Enrolled successfully", courseId, course.getCourseName());
    }

    @Override
    @Transactional
    public void unenroll(String userEmail, Long courseId) {
        User   user   = getUser(userEmail);
        Course course = getCourse(courseId);

        uceRepository.findByUserAndCourse(user, course).ifPresent(uce -> {

            // 1. Delete UserCourseTestQuestionAttempt records for this user+course,
            //    then delete the Attempt rows they referenced.
            List<UserCourseTestQuestionAttempt> attempts =
                    uctqaRepository.findByCourseAndUser(course, user);

            if (!attempts.isEmpty()) {
                // Collect distinct attempt IDs before deleting the junction rows
                Set<Long> attemptIds = attempts.stream()
                        .map(a -> a.getAttemptId())
                        .collect(Collectors.toSet());

                // Delete junction rows first (child)
                uctqaRepository.deleteAll(attempts);

                // Delete Attempt rows (parent)
                attemptRepository.deleteByAttemptIds(attemptIds);
            }

            // 2. Delete UserCourseTestQuestion records for this user+course
            List<UserCourseTestQuestion> uctqs =
                    uctqRepository.findByCourseAndUser(course, user);
            if (!uctqs.isEmpty()) {
                uctqRepository.deleteAll(uctqs);
            }

            // 3. Delete UserCourseTest records for this user+course
            List<UserCourseTest> ucts =
                    uctRepository.findByCourseAndUser(course, user);
            if (!ucts.isEmpty()) {
                uctRepository.deleteAll(ucts);
            }

            // 4. Delete UserCourseResourceTag rows for resources this user has in this course,
            //    then delete the UserCourseResource rows themselves.
            List<UserCourseResource> ucrs = ucrRepository.findByUserAndCourse(user, course);
            if (!ucrs.isEmpty()) {
                Set<Long> resourceIds = ucrs.stream()
                        .map(r -> r.getResource().getResourceId())
                        .collect(Collectors.toSet());
                ucrtRepository.deleteByResourceIdsAndUser(resourceIds, user.getUserId());
                ucrRepository.deleteAll(ucrs);
            }

            // 5. Delete the UserCourseEnrollment junction row, then the Enrollment row
            uceRepository.delete(uce);
            enrollmentRepository.deleteById(uce.getEnrollmentId());
        });
    }

    @Override
    public Set<Long> getEnrolledCourseIds(String userEmail) {
        User user = getUser(userEmail);
        return uceRepository.findByUser(user)
                .stream()
                .map(uce -> uce.getCourse().getCourseId())
                .collect(Collectors.toSet());
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    private User   getUser(String email) { return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found: "   + email)); }
    private Course getCourse(Long id)    { return courseRepository.findById(id).orElseThrow(() -> new RuntimeException("Course not found: " + id)); }
}