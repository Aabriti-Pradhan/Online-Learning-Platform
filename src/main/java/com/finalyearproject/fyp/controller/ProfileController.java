package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    /**
     * Handle profile update form submission.
     * Redirects back to the referring page (the sidebar modal closes after redirect).
     */
    @PostMapping("/update")
    public String updateProfile(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam(defaultValue = "/") String redirectUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String email = YourCoursesController.extractEmail(authentication);
        if (email == null) {
            return "redirect:/login";
        }

        // Validate password confirmation
        if (password != null && !password.isBlank()) {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("profileError", "Passwords do not match.");
                return "redirect:" + redirectUrl;
            }
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("profileError", "Password must be at least 6 characters.");
                return "redirect:" + redirectUrl;
            }
        }

        try {
            userService.updateProfile(email, username, password);
            redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileError", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:" + redirectUrl;
    }
}