package com.finalyearproject.fyp.controller;

import com.finalyearproject.fyp.dto.LoginDTO;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(Model model, HttpSession session, Authentication authentication) {
        System.out.println("SESSION ID (select-role GET): " + session.getId());
        System.out.println("ROLE IN SESSION: " + session.getAttribute("selectedRole"));
        System.out.println("AUTH OBJECT: " + authentication);

        if (authentication != null) {
            System.out.println("AUTH CLASS: " + authentication.getClass());
            System.out.println("PRINCIPAL: " + authentication.getPrincipal());
            System.out.println("IS AUTHENTICATED: " + authentication.isAuthenticated());
        } else {
            System.out.println("AUTH IS NULL");
        }

        model.addAttribute("page", "login");
        model.addAttribute("loginDTO", new LoginDTO("",""));
        return "login/index";
    }

//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @PostMapping("/login")
//    public String loginUser(@Valid @ModelAttribute("loginDTO") LoginDTO dto,
//                            BindingResult result,
//                            Model model) {
//
//        if (result.hasErrors()) {
//            System.out.println("failed in before if");
//            return "login/index";
//        }
//
//        User user = userService.findByEmail(dto.email());
//        if (user == null || !userService.checkPassword(dto.password(), user.getPassword())) {
//            model.addAttribute("error", "Invalid email or password");
//            System.out.println("failed in after if");
//            return "login/index";
//        }
//
//        UsernamePasswordAuthenticationToken authToken =
//                new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
//        Authentication auth = authenticationManager.authenticate(authToken);
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        System.out.println("login should be successful");
//        return "redirect:/your-resources";
//    }
}
