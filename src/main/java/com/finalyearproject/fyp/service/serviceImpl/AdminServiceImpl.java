package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.dto.AdminCourseDTO;
import com.finalyearproject.fyp.dto.AdminStatsDTO;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourse;
import com.finalyearproject.fyp.repository.*;
import com.finalyearproject.fyp.service.AdminService;
import com.finalyearproject.fyp.service.CourseService;
import com.finalyearproject.fyp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository                 userRepository;
    private final CourseRepository               courseRepository;
    private final EnrollmentRepository           enrollmentRepository;
    private final UserCourseRepository           userCourseRepository;
    private final UserCourseEnrollmentRepository uceRepository;
    private final ChapterRepository              chapterRepository;
    private final TestRepository                 testRepository;
    private final CourseService                  courseService;
    private final NotificationService            notificationService;

    // Used to get the Spring proxy of this bean so @Transactional on
    // deleteCourseInNewTransaction() is actually honoured on a self-call.
    private final ApplicationContext             applicationContext;

    @Override
    public AdminStatsDTO getStats() {
        long totalUsers       = userRepository.count();
        long totalStudents    = userRepository.countByRole("STUDENT");
        long totalTeachers    = userRepository.countByRole("TEACHER");
        long totalCourses     = courseRepository.count();
        long totalEnrollments = enrollmentRepository.count();
        long totalTests       = testRepository.count();
        return new AdminStatsDTO(
                totalUsers, totalStudents, totalTeachers,
                totalCourses, totalEnrollments, totalTests
        );
    }

    @Override
    public List<User> getAllUsers(String roleFilter, String search) {
        String role = (roleFilter != null && !roleFilter.isBlank()) ? roleFilter : null;
        String q    = (search     != null && !search.isBlank())     ? search     : null;
        return userRepository.searchUsers(role, q);
    }

    @Override
    @Transactional
    public User setUserActive(Long userId, boolean active) {
        User user = getUser(userId);
        user.setActive(active);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User setUserRole(Long userId, String role) {
        if (!List.of("STUDENT", "TEACHER", "ADMIN").contains(role)) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        User user = getUser(userId);
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public List<AdminCourseDTO> getAllCoursesForAdmin() {
        return courseRepository.findAll().stream().map(course -> {
            List<UserCourse> owners = userCourseRepository.findByCourse(course);
            String teacherName  = owners.isEmpty() ? "—" : owners.get(0).getUser().getUsername();
            String teacherEmail = owners.isEmpty() ? "—" : owners.get(0).getUser().getEmail();
            long enrollments    = uceRepository.findByCourse(course).size();
            long chapters       = chapterRepository.countByCourse(course);
            return new AdminCourseDTO(
                    course.getCourseId(), course.getCourseName(), course.getCourseDesc(),
                    teacherName, teacherEmail, enrollments, chapters
            );
        }).toList();
    }

    @Override
    public void deleteCourseAsAdmin(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found: " + courseId));

        String     courseName = course.getCourseName();
        List<Long> teacherIds = userCourseRepository.findByCourse(course)
                .stream()
                .map(uc -> uc.getUser().getUserId())
                .toList();

        AdminServiceImpl proxy = applicationContext.getBean(AdminServiceImpl.class);
        proxy.deleteCourseInNewTransaction(courseId);

        if (!teacherIds.isEmpty()) {
            notificationService.sendToUsers(
                    teacherIds,
                    "Course removed by admin",
                    "Your course \"" + courseName + "\" has been removed by an administrator.",
                    "ADMIN_COURSE_DELETION",
                    null
            );
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteCourseInNewTransaction(Long courseId) {
        courseService.deleteCourse(courseId);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }
}