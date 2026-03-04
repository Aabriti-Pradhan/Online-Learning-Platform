package com.finalyearproject.fyp.config;

import com.finalyearproject.fyp.entity.Course;
import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.entity.UserCourse;
import com.finalyearproject.fyp.repository.UserCourseRepository;
import com.finalyearproject.fyp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalController {

    private final UserRepository userRepository;
    private final UserCourseRepository userCourseRepository;

    @ModelAttribute
    public void addUserCourses(Model model, Authentication authentication) {

        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauthUser) {

            String email = oauthUser.getAttribute("email");

            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {

                List<UserCourse> userCourses =
                        userCourseRepository.findByUser(user);

                List<Course> courses = userCourses.stream()
                        .map(UserCourse::getCourse)
                        .toList();

                model.addAttribute("userCourses", courses);
            }
        }
    }
}