
package com.example.GATEPath.Controller;

import com.example.GATEPath.Service.PasswordResetService;

import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ForgotPasswordController {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordController(
            PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @RequestParam("email") String email,
            RedirectAttributes redirectAttributes) {

        try {
            passwordResetService.sendResetLink(email);
        } catch (MailException ex) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Email could not be sent. Please check the email configuration and try again."
            );
            return "redirect:/forgot-password";
        }

        redirectAttributes.addFlashAttribute(
                "message",
                "If an account exists for that email, a password reset link has been sent."
        );

        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String showResetPasswordPage(
            @RequestParam(value = "token", required = false) String token,
            Model model) {

        if (!passwordResetService.isValidToken(token)) {
            model.addAttribute("validToken", false);
            return "reset-password";
        }

        model.addAttribute("validToken", true);
        model.addAttribute("token", token);

        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @RequestParam("token") String token,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {

        if (password == null || password.length() < 8
                || password.length() > 72) {
            model.addAttribute("validToken",
                    passwordResetService.isValidToken(token));
            model.addAttribute("token", token);
            model.addAttribute("error",
                    "Password must contain between 8 and 72 characters.");
            return "reset-password";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("validToken",
                    passwordResetService.isValidToken(token));
            model.addAttribute("token", token);
            model.addAttribute("error",
                    "Passwords do not match.");
            return "reset-password";
        }

        boolean success =
                passwordResetService.resetPassword(token, password);

        if (!success) {
            model.addAttribute("validToken", false);
            model.addAttribute("error",
                    "This reset link is invalid or expired. Please request a new one.");
            return "reset-password";
        }

        return "redirect:/login?resetSuccess";
    }
}
