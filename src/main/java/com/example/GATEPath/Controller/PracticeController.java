
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

        // 1. Fetch subjects available in the question bank.
        List<String> subjects =
                questionService.getAvailableSubjects();

        // 2. Validate the selected subject.
        String selectedSubject =
                subject == null ? "" : subject.trim();

        if (!subjects.contains(selectedSubject)) {
            selectedSubject = subjects.isEmpty()
                    ? ""
                    : subjects.get(0);
        }

        // 3. Fetch topics for the selected subject.
        List<String> topics = selectedSubject.isBlank()
                ? new ArrayList<>()
                : questionService.getAvailableTopics(selectedSubject);

        // 4. Questions are fetched only when practice starts.
        List<Question> questions = new ArrayList<>();

        if (start) {

            // Require login before starting a practice session.
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication.getName() == null
                    || authentication.getName().isBlank()) {

                return "redirect:/login";
            }

            // Do not start a session without a valid subject.
            if (selectedSubject.isBlank()) {
                model.addAttribute("practiceMode", false);
                addSetupAttributes(
                        model, subjects, selectedSubject, topics
                );
                return "practice";
            }

            // Find the currently logged-in user.
            String email = authentication.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new IllegalStateException(
                                    "Logged-in user was not found."
                            ));

            // Fetch up to 20 unused questions for this user
            // and selected subject.
            questions =
                    questionService.getPracticeQuestionsForUser(
                            user.getId(),
                            selectedSubject,
                            "ALL",
                            "ALL",
                            PRACTICE_QUESTION_LIMIT
                    );
        }

        // 5. Supply all required attributes to Thymeleaf.
        model.addAttribute("practiceMode", start);
        addSetupAttributes(
                model, subjects, selectedSubject, topics
        );

        model.addAttribute("questions", questions);
        model.addAttribute(
                "questionLimit", PRACTICE_QUESTION_LIMIT
        );

        model.addAttribute(
                "noQuestionsAvailable",
                start && questions.isEmpty()
        );

        return "practice";
    }

    private void addSetupAttributes(
            Model model,
            List<String> subjects,
            String selectedSubject,
            List<String> topics) {

        model.addAttribute("subjects", subjects);
        model.addAttribute("selectedSubject", selectedSubject);
        model.addAttribute("topics", topics);
    }
}
