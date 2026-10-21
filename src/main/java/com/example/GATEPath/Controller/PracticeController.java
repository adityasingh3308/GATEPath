package com.example.GATEPath.Controller;

import com.example.GATEPath.Entity.Question;
import com.example.GATEPath.Service.QuestionService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.ArrayList;

@Controller
public class PracticeController {

    private final QuestionService questionService;

    public PracticeController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping("/practice")
    public String practicePage(
            @RequestParam(defaultValue = "false") boolean start,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "ALL") String subtopic,
            @RequestParam(defaultValue = "ALL") String questionType,
            @RequestParam(defaultValue = "10") String limit,
            Model model) {

        List<String> subjects =
                questionService.getAvailableSubjects();

        String selectedSubject = subject;

        if (selectedSubject == null
                || selectedSubject.isBlank()) {
            selectedSubject = subjects.isEmpty()
                    ? ""
                    : subjects.get(0);
        }

        List<String> subtopics =
                questionService.getAvailableSubtopics(selectedSubject);

        String selectedSubtopic =
                subtopic == null || subtopic.isBlank()
                        ? "ALL"
                        : subtopic;

        String selectedType =
                questionType == null || questionType.isBlank()
                        ? "ALL"
                        : questionType.toUpperCase();

        String selectedLimit =
                limit == null || limit.isBlank()
                        ? "10"
                        : limit.toUpperCase();

        int questionLimit;

        if ("ALL".equals(selectedLimit)) {
            questionLimit = 0;
        } else {
            try {
                questionLimit = Integer.parseInt(selectedLimit);

                if (questionLimit != 10
                        && questionLimit != 25
                        && questionLimit != 50) {
                    questionLimit = 10;
                    selectedLimit = "10";
                }
            } catch (NumberFormatException exception) {
                questionLimit = 10;
                selectedLimit = "10";
            }
        }

        List<Question> questions = new ArrayList<>();

        if (start) {
            questions = questionService.getPracticeQuestions(
                    selectedSubject,
                    selectedSubtopic,
                    selectedType,
                    questionLimit
            );
        }

        model.addAttribute("practiceMode", start);
        model.addAttribute("subjects", subjects);
        model.addAttribute("subtopics", subtopics);
        model.addAttribute("selectedSubject", selectedSubject);
        model.addAttribute("selectedSubtopic", selectedSubtopic);
        model.addAttribute("selectedType", selectedType);
        model.addAttribute("selectedLimit", selectedLimit);
        model.addAttribute("questions", questions);

        return "practice";
    }
}