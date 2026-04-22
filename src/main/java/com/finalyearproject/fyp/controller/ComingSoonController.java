package com.finalyearproject.fyp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComingSoonController {

    @GetMapping("/coming-soon")
    public String comingSoon(Model model, HttpServletRequest request) {

        model.addAttribute("currentPath", request.getRequestURI());
        return "comingSoon/index";
    }
}
