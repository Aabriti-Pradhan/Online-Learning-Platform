package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.*;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println("OAuth Service Initialized");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request)
            throws OAuth2AuthenticationException {

        System.out.println("Inside method");

        OAuth2User oauthUser = super.loadUser(request);

        System.out.println("GOOGLE USER INFO: " + oauthUser.getAttributes());

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from Google");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setUsername(name);
            user.setRole("STUDENT");

            userRepository.save(user);

            System.out.println("New Google user saved: " + email);
        } else {
            System.out.println("Existing user logged in: " + email);
        }

        return oauthUser;
    }
}
