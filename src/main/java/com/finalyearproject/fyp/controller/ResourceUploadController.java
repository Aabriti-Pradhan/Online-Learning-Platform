package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.Resource;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.UserRepository;
import com.finalyearproject.fyp.service.ResourceService;
import com.finalyearproject.fyp.controller.YourCoursesController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Phase 1 — Replaces DriveUploadController.
 * All uploads go to local disk via LocalFileStorageService.
 */
@Controller
@RequiredArgsConstructor
public class ResourceUploadController {

    private final ResourceService resourceService;
    private final UserRepository  userRepository;

    /** Upload a PDF into a course. Called from the course resources page. */
    @PostMapping("/upload-pdf/{courseId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> uploadPdf(
            @PathVariable Long courseId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        try {
            User user = resolveUser(authentication);
            Resource saved = resourceService.savePdf(user.getUserId(), courseId, file);

            return ResponseEntity.ok(Map.of(
                    "resourceId",   saved.getResourceId(),
                    "resourceName", saved.getResourceName(),
                    "resourcePath", saved.getResourcePath(),
                    "resourceType", saved.getResourceType()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Delete a resource (PDF or Note) by its ID. */
    @DeleteMapping("/delete-resource/{resourceId}")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> deleteResource(@PathVariable Long resourceId) {
        try {
            resourceService.deleteResource(resourceId);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User resolveUser(Authentication auth) {
        String email = YourCoursesController.extractEmail(auth);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }
}