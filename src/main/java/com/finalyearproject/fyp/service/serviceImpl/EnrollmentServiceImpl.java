package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.EnrollmentDTO;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.EnrollmentService;
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

    private final CourseRepository               courseRepository;
    private final UserRepository                 userRepository;
    private final EnrollmentRepository           enrollmentRepository;
    private final UserCourseEnrollmentRepository uceRepository;

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

        return new EnrollmentDTO("Enrolled successfully", courseId, course.getCourseName());
    }

    @Override
    @Transactional
    public void unenroll(String userEmail, Long courseId) {
        User   user   = getUser(userEmail);
        Course course = getCourse(courseId);

        uceRepository.findByUserAndCourse(user, course).ifPresent(uce -> {
            enrollmentRepository.deleteById(uce.getEnrollmentId());
            uceRepository.delete(uce);
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

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }
}