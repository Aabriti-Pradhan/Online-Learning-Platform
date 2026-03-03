package com.finalyearproject.fyp.service.serviceImpl;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.*;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.*;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest request)
            throws OAuth2AuthenticationException {

        OidcUser oidcUser = super.loadUser(request);

        String email = oidcUser.getEmail();

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return oidcUser;
        }

        Set<GrantedAuthority> authorities = new HashSet<>(oidcUser.getAuthorities());

        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));

        return new DefaultOidcUser(
                authorities,
                oidcUser.getIdToken(),
                oidcUser.getUserInfo()
        );
    }
}