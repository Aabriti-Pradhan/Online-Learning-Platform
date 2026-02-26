package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.service.serviceImpl.GoogleDriveService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Controller;

@Controller
public class DriveUploadController {

    private final GoogleDriveService driveService;

    public DriveUploadController(GoogleDriveService driveService) {
        this.driveService = driveService;
    }

    @PostMapping("/upload-drive")
    @ResponseBody
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal OAuth2User principal) throws Exception {

        System.out.println("Received: " + file.getOriginalFilename());

        driveService.uploadToDrive(file);

        return "Upload successful!";
    }
}