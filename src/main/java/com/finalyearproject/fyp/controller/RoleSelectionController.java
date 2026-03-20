package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.service.UserService;
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

@Controller
@RequiredArgsConstructor
public class RoleSelectionController {

    private final UserService userService;

    @GetMapping("/select-role")
    public String selectRolePage(Model model) {
        model.addAttribute("page", "select-role");
        return "selectRole/index";
    }

    @PostMapping("/select-role")
    public String selectRole(@RequestParam String role,
                             Authentication authentication,
                             HttpSession session) {

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            session.setAttribute("selectedRole", role);
            return "redirect:/register";
        }

        String email = null;
        String name  = null;

        if (authentication.getPrincipal() instanceof OidcUser u)  { email = u.getEmail(); name = u.getFullName(); }
        else if (authentication.getPrincipal() instanceof OAuth2User u) { email = u.getAttribute("email"); name = u.getAttribute("name"); }

        if (email != null) {
            userService.registerOAuthUser(email, name, role);
        }

        return "redirect:/your-courses";
    }
}