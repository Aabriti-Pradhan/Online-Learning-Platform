package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.EnrollmentDTO;
import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/repository")
    public String repository(HttpServletRequest request, Model model, Authentication authentication) {
        List<Course> allCourses = enrollmentService.getAllCourses();
        model.addAttribute("courses",     allCourses);
        model.addAttribute("currentPath", request.getRequestURI());

        if (authentication != null && authentication.isAuthenticated()) {
            String   email       = YourCoursesController.extractEmail(authentication);
            Set<Long> enrolledIds = enrollmentService.getEnrolledCourseIds(email);
            model.addAttribute("enrolledCourseIds", enrolledIds);
        }

        return "repository/index";
    }

    @PostMapping("/enroll/{courseId}")
    @ResponseBody
    public ResponseEntity<?> enroll(@PathVariable Long courseId, Authentication authentication) {
        try {
            String        email = YourCoursesController.extractEmail(authentication);
            EnrollmentDTO dto   = enrollmentService.enroll(email, courseId);
            return ResponseEntity.ok(Map.of(
                    "message",    dto.message(),
                    "courseId",   dto.courseId(),
                    "courseName", dto.courseName()
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/unenroll/{courseId}")
    @ResponseBody
    public ResponseEntity<?> unenroll(@PathVariable Long courseId, Authentication authentication) {
        String email = YourCoursesController.extractEmail(authentication);
        enrollmentService.unenroll(email, courseId);
        return ResponseEntity.ok(Map.of("message", "Unenrolled successfully"));
    }
}