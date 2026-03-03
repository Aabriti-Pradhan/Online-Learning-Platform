package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class YourCoursesController {
    @GetMapping("/your-courses")
    public String coursesPage(Model model) {

        model.addAttribute("currentPath", "/your-courses");
        return "yourCourses/index";
    }
}
