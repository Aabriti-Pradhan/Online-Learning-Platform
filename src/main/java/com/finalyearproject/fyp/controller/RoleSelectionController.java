package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
                             Authentication authentication) {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        assert oauthUser != null;
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        User user = new User();
        user.setEmail(email);
        user.setUsername(name);
        user.setRole(role);

        userRepository.save(user);

        System.out.println("user saved !!");

        if(Objects.equals(role, "TEACHER")){
            return "yourCourses/index";
        }
        else {
            return "yourResources/index";
        }
    }
}