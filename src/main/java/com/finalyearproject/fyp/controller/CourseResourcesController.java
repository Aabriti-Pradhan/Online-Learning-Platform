package com.finalyearproject.fyp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CourseResourcesController {
    @GetMapping("/course-resources")
    public String resourcesPage(HttpServletRequest request, Model model) {
        model.addAttribute("page", "resources");
        model.addAttribute("currentPath", request.getRequestURI());
        return "courseResources/index";
    }
}
