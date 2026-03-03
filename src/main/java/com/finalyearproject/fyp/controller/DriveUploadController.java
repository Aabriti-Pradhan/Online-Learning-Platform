package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.ResourceService;
import com.finalyearproject.fyp.service.serviceImpl.GoogleDriveService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;

@Controller
public class DriveUploadController {

    private final GoogleDriveService driveService;
    private final ResourceService resourceService;
    private final UserRepository userRepository;

    public DriveUploadController(GoogleDriveService driveService,
                                 ResourceService resourceService,
                                 UserRepository userRepository) {
        this.driveService = driveService;
        this.resourceService = resourceService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload-drive")
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal OAuth2User principal) throws Exception {

        System.out.println("Received: " + file.getOriginalFilename());

        String fileId = driveService.uploadToDrive(file);

        String email = principal.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        resourceService.savePdf(
                user.getUserId(),
                1L,
                fileId,
                file.getOriginalFilename()
        );

        return "Upload successful!";
    }
}