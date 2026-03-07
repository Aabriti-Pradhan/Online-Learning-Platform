package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.repository.CourseRepository;
import com.finalyearproject.fyp.service.ResourceService;
import com.finalyearproject.fyp.service.serviceImpl.CourseDriveService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class YourCoursesController {

    private final CourseRepository courseRepository;
    private final CourseDriveService courseDriveService;
    private final ResourceService resourceService;

    @GetMapping("/your-courses")
    public String coursesPage(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        model.addAttribute("currentPath", "/your-courses");
        return "yourCourses/index";
    }

    @GetMapping("/your-courses/{courseId}/resources")
    public String courseResources(@PathVariable Long courseId,
                                  Model model,
                                  OAuth2AuthenticationToken oauthToken,
                                  HttpServletRequest request) throws Exception {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // Get files inside this Drive folder
        List<com.google.api.services.drive.model.File> files =
                courseDriveService.listFilesInsideFolder(
                        course.getDriveFolderId(),
                        oauthToken
                );

        List<Resource> pdfs = resourceService.getUserResources(oauthToken.getPrincipal().getAttribute("email"));

        model.addAttribute("pdfs", pdfs);
        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("course", course);
        model.addAttribute("files", files);

        return "courseResources/index";
    }

}
