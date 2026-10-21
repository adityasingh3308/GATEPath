
package com.example.GATEPath.Controller;

import com.example.GATEPath.Entity.User;
import com.example.GATEPath.Repository.UserRepository;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Year;
import java.util.Set;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    private static final Set<String> AVAILABLE_PAPERS = Set.of(
            "CS", "DA", "EC", "EE", "ME", "CE"
    );

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = getLoggedInUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("profile", user);
        model.addAttribute("editMode", false);
        model.addAttribute("dashboardLayout", true);

        return "admin/profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Authentication authentication, Model model) {
        User user = getLoggedInUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("profile", user);
        model.addAttribute("editMode", true);
        model.addAttribute("dashboardLayout", true);
        model.addAttribute("availablePapers", AVAILABLE_PAPERS);
        model.addAttribute("currentYear", Year.now().getValue());

        return "admin/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            Authentication authentication,
            @ModelAttribute("profile") User formUser,
            RedirectAttributes redirectAttributes) {

        User user = getLoggedInUser(authentication);

        if (user == null) {
            return "redirect:/login";
        }

        String fullName = formUser.getFullName();

        if (fullName == null || fullName.isBlank()
                || fullName.trim().length() > 100) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please enter a valid full name."
            );

            return "redirect:/profile/edit";
        }

        Set<String> selectedPapers = formUser.getGatePapers();

        if (selectedPapers == null) {
            selectedPapers = Set.of();
        }

        if (!AVAILABLE_PAPERS.containsAll(selectedPapers)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Please select valid GATE papers."
            );

            return "redirect:/profile/edit";
        }

        user.setFullName(fullName.trim());
        user.setPhone(clean(formUser.getPhone()));
        user.setCollege(clean(formUser.getCollege()));
        user.setDegree(clean(formUser.getDegree()));
        user.setBranch(clean(formUser.getBranch()));
        user.setGraduationYear(formUser.getGraduationYear());
        user.setTargetYear(formUser.getTargetYear());
        user.setGatePapers(selectedPapers);

        // Email, password and role are deliberately not changed.
        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Profile updated successfully!"
        );

        return "redirect:/profile";
    }

    private User getLoggedInUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }

        return userRepository.findByEmail(authentication.getName())
                .orElse(null);
    }

    private String clean(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
