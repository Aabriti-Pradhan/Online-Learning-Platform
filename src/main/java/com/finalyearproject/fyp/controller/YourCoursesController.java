package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourse;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.repository.UserCourseRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class YourCoursesController {

    private final CourseRepository     courseRepository;
    private final UserRepository       userRepository;
    private final UserCourseRepository userCourseRepository;
    private final ResourceService      resourceService;

    @GetMapping("/your-courses")
    public String coursesPage(Model model, Authentication authentication) {
        User user = resolveUser(authentication);

        List<Course> courses = userCourseRepository.findByUser(user)
                .stream().map(UserCourse::getCourse).toList();

        model.addAttribute("courses",     courses);
        model.addAttribute("currentPath", "/your-courses");
        return "yourCourses/index";
    }

    @GetMapping("/your-courses/{courseId}/resources")
    public String courseDashboard(@PathVariable Long courseId,
                                  Model model,
                                  HttpServletRequest request) {

        Course         course    = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        List<Resource> resources = resourceService.getCourseResources(courseId);

        long pdfCount  = resources.stream().filter(r -> "PDF" .equalsIgnoreCase(r.getResourceType())).count();
        long noteCount = resources.stream().filter(r -> "Note".equalsIgnoreCase(r.getResourceType())).count();

        model.addAttribute("course",      course);
        model.addAttribute("resources",   resources);
        model.addAttribute("pdfCount",    pdfCount);
        model.addAttribute("noteCount",   noteCount);
        model.addAttribute("currentPath", request.getRequestURI());
        return "courseResources/index";
    }

    // ── helper — works for both OAuth2 (Google) and form login ───────────────

    private User resolveUser(Authentication authentication) {
        String email = extractEmail(authentication);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }

    /**
     * authentication.getName() returns the Google subject ID for OAuth2 users,
     * NOT the email. We must pull the email from the OAuth2 attributes instead.
     */
    public static String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof OidcUser oidcUser) {
            return oidcUser.getEmail();
        }
        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }
        // Form login — getName() is the email
        return authentication.getName();
    }
}