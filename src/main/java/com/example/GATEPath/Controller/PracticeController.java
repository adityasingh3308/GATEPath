
package com.example.GATEPath.Controller;

import com.example.GATEPath.Entity.Question;
import com.example.GATEPath.Entity.User;
import com.example.GATEPath.Repository.UserRepository;
import com.example.GATEPath.Service.QuestionService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class PracticeController {

    private static final int PRACTICE_QUESTION_LIMIT = 20;

    private final QuestionService questionService;
    private final UserRepository userRepository;

    public PracticeController(
            QuestionService questionService,
            UserRepository userRepository) {
        this.questionService = questionService;
        this.userRepository = userRepository;
    }

    @GetMapping("/practice")
    public String practicePage(
            @RequestParam(defaultValue = "false") boolean start,
            @RequestParam(required = false) String subject,
            Authentication authentication,
            Model model) {

        // Get available subjects.
        List<String> subjects = questionService.getAvailableSubjects();

        // Validate selected subject.
        String selectedSubject = subject == null ? "" : subject.trim();

        if (!subjects.contains(selectedSubject)) {
            selectedSubject = subjects.isEmpty() ? "" : subjects.get(0);
        }

        // Get topics for the selected subject.
        List<String> topics = selectedSubject.isBlank()
                ? new ArrayList<>()
                : questionService.getAvailableTopics(selectedSubject);

        List<Question> questions = new ArrayList<>();

        // Assign questions only when Start Practice is clicked.
        if (start && !selectedSubject.isBlank()) {

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getName() == null) {
                return "redirect:/login";
            }

            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Logged-in user was not found."));

            questions = questionService.getPracticeQuestionsForUser(
                    user.getId(),
                    selectedSubject,
                    "ALL",
                    "ALL",
                    PRACTICE_QUESTION_LIMIT
            );
        }

        model.addAttribute("practiceMode", start);
        model.addAttribute("subjects", subjects);
        model.addAttribute("selectedSubject", selectedSubject);
        model.addAttribute("topics", topics);
        model.addAttribute("questions", questions);
        model.addAttribute("questionLimit", PRACTICE_QUESTION_LIMIT);

        // Display a message if the user has no unused questions left.
        model.addAttribute(
                "noQuestionsAvailable",
                start && questions.isEmpty()
        );

        return "practice";
    }
}
