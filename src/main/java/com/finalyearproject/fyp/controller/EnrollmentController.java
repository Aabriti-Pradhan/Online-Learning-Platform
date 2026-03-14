package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class EnrollmentController {

    private final CourseRepository               courseRepository;
    private final UserRepository                 userRepository;
    private final EnrollmentRepository           enrollmentRepository;
    private final UserCourseEnrollmentRepository uceRepository;

    // ── Public course browser — works without login ───────────────────────────

    @GetMapping("/repository")
    public String repository(HttpServletRequest request, Model model, Authentication authentication) {
        List<Course> allCourses = courseRepository.findAll();
        model.addAttribute("courses", allCourses);
        model.addAttribute("currentPath", request.getRequestURI());

        // If logged in, pass set of already-enrolled course IDs so we can show enrolled badge
        if (authentication != null && authentication.isAuthenticated()) {
            String email = YourCoursesController.extractEmail(authentication);
            userRepository.findByEmail(email).ifPresent(user -> {
                Set<Long> enrolledIds = uceRepository.findByUser(user)
                        .stream()
                        .map(uce -> uce.getCourse().getCourseId())
                        .collect(Collectors.toSet());
                model.addAttribute("enrolledCourseIds", enrolledIds);
            });
        }

        return "repository/index";
    }

    // ── Enroll in a course ────────────────────────────────────────────────────

    @PostMapping("/enroll/{courseId}")
    @ResponseBody
    public ResponseEntity<?> enroll(@PathVariable Long courseId,
                                    Authentication authentication) {

        String email = YourCoursesController.extractEmail(authentication);
        User   user  = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Check not already enrolled
        if (uceRepository.findByUserAndCourse(user, course).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Already enrolled"));
        }

        // Save Enrollment record
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus("ACTIVE");
        enrollmentRepository.save(enrollment);

        // Save join row
        UserCourseEnrollment uce = new UserCourseEnrollment();
        uce.setEnrollmentId(enrollment.getEnrollmentId());
        uce.setCourseId(course.getCourseId());
        uce.setUserId(user.getUserId());
        uceRepository.save(uce);

        return ResponseEntity.ok(Map.of(
                "message",    "Enrolled successfully",
                "courseId",   courseId,
                "courseName", course.getCourseName()
        ));
    }

    // ── Unenroll from a course ────────────────────────────────────────────────

    @DeleteMapping("/unenroll/{courseId}")
    @ResponseBody
    public ResponseEntity<?> unenroll(@PathVariable Long courseId,
                                      Authentication authentication) {

        String email = YourCoursesController.extractEmail(authentication);
        User   user  = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        uceRepository.findByUserAndCourse(user, course).ifPresent(uce -> {
            enrollmentRepository.deleteById(uce.getEnrollmentId());
            uceRepository.delete(uce);
        });

        return ResponseEntity.ok(Map.of("message", "Unenrolled successfully"));
    }
}