package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.RegisterDTO;
import com.finalyearproject.fyp.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        System.out.println("SESSION ID (register): " + session.getId());
        System.out.println("ROLE IN SESSION: " + session.getAttribute("selectedRole"));

        if (session.getAttribute("selectedRole") == null) {
            return "redirect:/select-role";
        }
        model.addAttribute("page", "register");
        model.addAttribute("registerDTO", new RegisterDTO("", "", "", "", ""));
        return "register/index";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult result,
            HttpSession session,
            Model model) {
        try {
            System.out.println("ROLE FROM DTO: " + dto.role());
            if (result.hasErrors()) {
                model.addAttribute("error", "Please correct the errors in the form");
                return "register/index";
            }
            if (!dto.password().equals(dto.confirmPassword())) {
                model.addAttribute("error", "Passwords do not match");
                return "register/index";
            }
            if (userService.isEmailTaken(dto.email())) {
                model.addAttribute("error", "Email already registered");
                return "register/index";
            }

            String role = (String) session.getAttribute("selectedRole");
            if (role == null) return "redirect:/select-role";

            RegisterDTO dtoWithRole = new RegisterDTO(
                    dto.username(),
                    dto.email(),
                    dto.password(),
                    dto.confirmPassword(),
                    role
            );

            userService.registerUser(dtoWithRole);
            session.removeAttribute("selectedRole");
            return "redirect:/login?success";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register/index";
        }
    }
}