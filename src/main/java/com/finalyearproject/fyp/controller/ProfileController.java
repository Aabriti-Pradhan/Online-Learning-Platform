package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            Authentication authentication) {

        String email = YourCoursesController.extractEmail(authentication);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Not authenticated"));
        }

        if (password != null && !password.isBlank()) {
            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of("message", "Passwords do not match"));
            }
            if (password.length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters"));
            }
        }

        try {
            userService.updateProfile(email, username, password);
            return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}