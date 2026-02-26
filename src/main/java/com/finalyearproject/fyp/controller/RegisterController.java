package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.RegisterDTO;
import com.finalyearproject.fyp.service.UserService;
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
    public String registerPage(Model model) {
        model.addAttribute("page", "register");
        model.addAttribute("registerDTO", new RegisterDTO("", "", "", "", ""));
        return "register/index";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDTO") RegisterDTO dto, BindingResult result, Model model) {
        try {
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
            userService.registerUser(dto);
            return "redirect:/login?success";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register/index";
        }
    }
}
