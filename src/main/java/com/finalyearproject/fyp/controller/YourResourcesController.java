package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.service.ResourceService;
import com.finalyearproject.fyp.service.serviceImpl.GoogleDriveService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.google.api.services.drive.model.File;
import java.util.List;

@Controller
public class YourResourcesController {

    private final GoogleDriveService googleDriveService;

    private final ResourceService resourceService;

    public YourResourcesController(GoogleDriveService driveService,
                                   ResourceService resourceService) {
        this.googleDriveService = driveService;
        this.resourceService = resourceService;
    }

    @GetMapping("/your-resources")
    public String resourcesPage(HttpServletRequest request,
                                Model model,
                                OAuth2AuthenticationToken auth) {

        List<Resource> pdfs = resourceService.getUserResources(auth.getPrincipal().getAttribute("email"));

        model.addAttribute("pdfs", pdfs);
        model.addAttribute("page", "resources");
        model.addAttribute("currentPath", request.getRequestURI());

        return "yourResources/index";
    }
}
