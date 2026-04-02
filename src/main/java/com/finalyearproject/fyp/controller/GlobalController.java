package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.service.CourseService;
import com.finalyearproject.fyp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {

    private final CourseService courseService;
    private final UserService   userService;

    @ModelAttribute
    public void addGlobalAttributes(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return;

        String email = YourCoursesController.extractEmail(authentication);
        if (email == null) return;

        User loggedInUser = userService.findByEmail(email);
        model.addAttribute("loggedInUser", loggedInUser);

        // Admins have no personal courses — skip the query to avoid empty noise
        if (loggedInUser != null && !"ADMIN".equals(loggedInUser.getRole())) {
            List<Course> courses = courseService.getCoursesForUser(email);
            model.addAttribute("userCourses", courses);
        }
    }
}