package com.finalyearproject.fyp.configuration;

import com.finalyearproject.fyp.entity.User;
import com.finalyearproject.fyp.service.UserService;
import com.finalyearproject.fyp.service.serviceImpl.CustomOidcUserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final UserService           userService;
    private final CustomOidcUserService customOidcUserService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/register", "/select-role", "/repository",
                                "/css/**", "/js/**", "/images/**", "/files/**",
                                "/discussion", "/discussion/**"
                        ).permitAll()
                        // Admin-only routes
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/login")
                        .userInfoEndpoint(user -> user.oidcUserService(customOidcUserService))
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
                            String email = oauthUser.getAttribute("email");

                            User existing = userService.findByEmail(email);

                            if (existing == null) {

                                HttpSession session = request.getSession();
                                String role = (String) session.getAttribute("selectedRole");

                                if (role == null) {
                                    response.sendRedirect("/select-role");
                                    return;
                                }

                                String name = oauthUser.getAttribute("name");
                                userService.registerOAuthUser(email, name, role);

                                session.removeAttribute("selectedRole");

                                response.sendRedirect("/login");
                                return;
                            }

                            // Existing user
                            if ("ADMIN".equals(existing.getRole())) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/your-courses");
                            }
                        })
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            // Redirect admin to /admin dashboard after login
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                            if (isAdmin) {
                                response.sendRedirect("/admin");
                            } else {
                                response.sendRedirect("/your-courses");
                            }
                        })
                        .failureUrl("/login?error")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = userService.findByEmail(email);
            if (user == null) throw new UsernameNotFoundException("User not found");
            // Block deactivated accounts from logging in
            if (!user.isActive()) throw new DisabledException("Account has been deactivated");
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword() == null ? "" : user.getPassword(),
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
            );
        };
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http,
                                             UserDetailsService uds) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(uds).passwordEncoder(passwordEncoder());
        return builder.build();
    }
}