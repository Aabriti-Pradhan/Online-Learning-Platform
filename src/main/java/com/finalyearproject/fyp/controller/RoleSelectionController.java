package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
public class RoleSelectionController {

    private final UserRepository userRepository;

    @GetMapping("/select-role")
    public String selectRolePage(Model model) {
        model.addAttribute("page", "select-role");
        return "selectRole/index";
    }

    @PostMapping("/select-role")
    public String selectRole(@RequestParam String role,
                             Authentication authentication,
                             HttpSession session) {

        // ── Manual registration (not yet logged in) ───────────────────────────
        // Just store role in session and redirect to registration form
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            session.setAttribute("selectedRole", role);
            return "redirect:/register";
        }

        // ── OAuth2 login (new Google user selecting role) ─────────────────────
        String email = null;
        String name  = null;

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            email = oidcUser.getEmail();
            name  = oidcUser.getFullName();
        } else if (authentication.getPrincipal() instanceof OAuth2User oauthUser) {
            email = oauthUser.getAttribute("email");
            name  = oauthUser.getAttribute("name");
        }

        if (email != null) {
            User user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setRole(role);
            userRepository.save(user);
        }

        return Objects.equals(role, "TEACHER")
                ? "redirect:/your-courses"
                : "redirect:/your-courses";
    }
}