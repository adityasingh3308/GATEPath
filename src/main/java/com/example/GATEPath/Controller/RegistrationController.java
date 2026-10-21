
package com.example.GATEPath.Controller;

import com.example.GATEPath.Service.UserService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RegistrationController {

    private final UserService userService;

    public RegistrationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            Model model) {

        if (fullName.trim().length() < 2) {
            model.addAttribute(
                "error", "Please enter your full name."
            );
            return "admin/register";
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute(
                "error", "Please enter a valid email address."
            );
            return "admin/register";
        }

        if (password.length() < 8) {
            model.addAttribute(
                "error", "Password must contain at least 8 characters."
            );
            return "admin/register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute(
                "error", "Passwords do not match."
            );
            return "admin/register";
        }

        try {
            userService.registerUser(fullName, email, password);
            return "redirect:/login?registered";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "admin/register";

        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            model.addAttribute(
                "error", "This email may already be registered. Please try another."
            );
            return "admin/register";
        }
    }
}
