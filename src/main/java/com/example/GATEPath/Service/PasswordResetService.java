
package com.example.GATEPath.Service;

import com.example.GATEPath.Entity.PasswordResetToken;
import com.example.GATEPath.Entity.User;
import com.example.GATEPath.Repository.PasswordResetTokenRepository;
import com.example.GATEPath.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.password-reset.expiry-minutes:30}")
    private long expiryMinutes;

    @Value("${app.base-url:http://localhost:2005}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String senderEmail;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Transactional
    public void sendResetLink(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email.trim());

        // Do not reveal whether an account exists.
        if (optionalUser.isEmpty()) {
            return;
        }

        User user = optionalUser.get();

        tokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = new PasswordResetToken(
                token,
                user,
                LocalDateTime.now().plusMinutes(expiryMinutes));

        tokenRepository.save(resetToken);

        String resetLink = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(user.getEmail());
        message.setSubject("GATEPath - Reset Your Password");
        message.setText(
                "Hello " + user.getFullName() + ",\n\n"
                        + "We received a request to reset your GATEPath password.\n\n"
                        + "Use this link to set a new password:\n"
                        + resetLink + "\n\n"
                        + "This link expires in " + expiryMinutes
                        + " minutes and can be used only once.\n\n"
                        + "If you did not request this, you can ignore this email.");

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            tokenRepository.delete(resetToken);
            throw ex;
        }
    }

    public boolean isValidToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        return tokenRepository.findByToken(token)
                .filter(resetToken -> resetToken.getExpiryDate()
                        .isAfter(LocalDateTime.now()))
                .isPresent();
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Optional<PasswordResetToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = optionalToken.get();

        if (!resetToken.getExpiryDate().isAfter(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            return false;
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Token cannot be reused.
        tokenRepository.delete(resetToken);

        return true;
    }
}
