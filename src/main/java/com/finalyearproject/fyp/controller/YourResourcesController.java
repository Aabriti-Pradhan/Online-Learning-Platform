package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.UserResourceDTO;
import com.finalyearproject.fyp.service.ResourceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class YourResourcesController {

    private final ResourceService resourceService;

    @GetMapping("/your-resources")
    public String resourcesPage(HttpServletRequest request,
                                Model model,
                                Authentication authentication) {

        String email = YourCoursesController.extractEmail(authentication);
        List<UserResourceDTO> resources = resourceService.getUserResourcesWithContext(email);

        model.addAttribute("pdfs",        resources);
        model.addAttribute("currentPath", request.getRequestURI());
        return "yourResources/index";
    }
}