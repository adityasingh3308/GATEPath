
package com.example.GATEPath.Service;

import com.example.GATEPath.Entity.User;
import com.example.GATEPath.Repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(
            String fullName,
            String email,
            String password) {

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException(
                "This email is already registered."
            );
        }

        User user = new User();
        user.setFullName(fullName.trim());
        user.setEmail(normalizedEmail);

        // Never store the original password
        user.setPassword(passwordEncoder.encode(password));

        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }
}
