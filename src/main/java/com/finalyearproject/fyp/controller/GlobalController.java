package com.finalyearproject.fyp.config;

import com.finalyearproject.fyp.controller.YourCoursesController;
import com.finalyearproject.fyp.entity.*;
import com.finalyearproject.fyp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {

    private final UserRepository               userRepository;
    private final UserCourseRepository         userCourseRepository;
    private final UserCourseEnrollmentRepository enrollmentRepository;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return;

        String email = YourCoursesController.extractEmail(authentication);
        if (email == null) return;

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return;

        model.addAttribute("loggedInUser", user);

        boolean isStudent = authentication.getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_STUDENT"));

        if (isStudent) {
            // Students see their ENROLLED courses in sidebar
            List<Course> enrolledCourses = enrollmentRepository.findByUser(user)
                    .stream()
                    .map(uce -> uce.getCourse())
                    .toList();
            model.addAttribute("userCourses", enrolledCourses);
        } else {
            // Teachers see their CREATED courses in sidebar
            List<Course> createdCourses = userCourseRepository.findByUser(user)
                    .stream()
                    .map(UserCourse::getCourse)
                    .toList();
            model.addAttribute("userCourses", createdCourses);
        }
    }
}