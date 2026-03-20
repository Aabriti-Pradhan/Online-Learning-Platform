package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.CourseDTO;
import com.finalyearproject.fyp.dto.CreateCourseRequest;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourse;
import com.finalyearproject.fyp.entity.UserCourseEnrollment;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository               courseRepository;
    private final UserRepository                 userRepository;
    private final UserCourseRepository           userCourseRepository;
    private final UserCourseEnrollmentRepository enrollmentRepository;
    private final UserCourseResourceRepository   ucrRepository;

    @Override
    @Transactional
    public CourseDTO createCourse(String userEmail, CreateCourseRequest request) {
        User user = getUser(userEmail);

        Course course = new Course();
        course.setCourseName(request.courseName());
        course.setCourseDesc(request.courseDesc());
        course.setCreatedAt(LocalDateTime.now());
        courseRepository.save(course);

        UserCourse uc = new UserCourse();
        uc.setUser(user);
        uc.setCourse(course);
        userCourseRepository.save(uc);

        return toDTO(course);
    }

    @Override
    @Transactional
    public CourseDTO updateCourse(Long courseId, CreateCourseRequest request) {
        Course course = getCourse(courseId);
        if (request.courseName() != null) course.setCourseName(request.courseName());
        if (request.courseDesc()  != null) course.setCourseDesc(request.courseDesc());
        courseRepository.save(course);
        return toDTO(course);
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = getCourse(courseId);
        ucrRepository.deleteByCourse(course);
        userCourseRepository.deleteByCourse(course);
        courseRepository.delete(course);
    }

    @Override
    public List<Course> getCoursesForUser(String userEmail) {
        User user = getUser(userEmail);

        List<Course> owned = userCourseRepository.findByUser(user)
                .stream().map(UserCourse::getCourse).toList();

        List<Course> enrolled = enrollmentRepository.findByUser(user)
                .stream().map(UserCourseEnrollment::getCourse).toList();

        List<Course> all = new ArrayList<>(owned);
        enrolled.forEach(c -> {
            if (all.stream().noneMatch(o -> o.getCourseId().equals(c.getCourseId())))
                all.add(c);
        });
        return all;
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }

    private CourseDTO toDTO(Course c) {
        return new CourseDTO(c.getCourseId(), c.getCourseName(), c.getCourseDesc());
    }
}