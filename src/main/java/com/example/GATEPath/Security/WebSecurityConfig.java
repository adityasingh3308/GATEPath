
package com.example.GATEPath.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .authorizeHttpRequests(auth -> auth
                // Public pages and static resources
                .requestMatchers(
                    "/",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/login",
                    "/register",
                    "/forgot-password",
                    "/reset-password",
                    "/syllabus",
                    "/study-material",
                    "/pyqs"
                ).permitAll()

                // Practice and other protected pages
                .requestMatchers(
                    "/practice",
                    "/practice/**",
                    "/mock-tests",
                    "/mock-tests/**",
                    "/profile",
                    "/my-progress"
                ).authenticated()

                // All other routes require authentication
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
