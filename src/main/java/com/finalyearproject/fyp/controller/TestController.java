package com.finalyearproject.fyp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

    @GetMapping("/create-test")
    public String createTestPage(HttpServletRequest request, Model model) {
        // Add any dynamic data here
//        model.addAttribute("user", getCurrentUser());
//        model.addAttribute("tests", testService.getPreviousTests());
        model.addAttribute("pageTitle", "Create Test");
        model.addAttribute("username", "Create Test");
        model.addAttribute("userEmail", "Create Test");
        model.addAttribute("currentPath", request.getRequestURI());
        return "createTest/index";
    }

    @GetMapping("/create-test/new")
    public String createNewTest() {
        // Handle new test creation
        return "redirect:/test-builder";
    }

    @GetMapping("/take-test")
    public String takeTestPage(HttpServletRequest request, Model model) {
        // Add any dynamic data here
//        model.addAttribute("user", getCurrentUser());
//        model.addAttribute("tests", testService.getPreviousTests());
        model.addAttribute("pageTitle", "Take Test");
        model.addAttribute("username", "Take Test");
        model.addAttribute("userEmail", "Take Test");
        model.addAttribute("currentPath", request.getRequestURI());
        return "takeTest/index";
    }
}