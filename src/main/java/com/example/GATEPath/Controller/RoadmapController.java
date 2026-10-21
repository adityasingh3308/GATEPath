
package com.example.GATEPath.Controller;


import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RoadmapController {

    @GetMapping("/roadmap")
    public String roadmap(
            @RequestParam(name = "paper", defaultValue = "CSE") String paper,
            Model model) {

        // Only CSE is available for now.
        // Other papers can be enabled when their roadmaps are ready.
        String selectedPaper = "CSE";

        List<PaperOption> paperOptions = List.of(
            new PaperOption(
                "CSE",
                "Computer Science and Information Technology (CSE)",
                true
            ),
            new PaperOption("DA", "Data Science and AI (DA)", true),
            new PaperOption("EC", "Electronics and Communication (EC)", false),
            new PaperOption("EE", "Electrical Engineering (EE)", false),
            new PaperOption("ME", "Mechanical Engineering (ME)", false),
            new PaperOption("CE", "Civil Engineering (CE)", false)
        );

        List<RoadmapPhase> phases = List.of(

            new RoadmapPhase(
                "01",
                "Understand GATE and Plan",
                "Start by understanding the exam, the syllabus and the preparation journey.",
                List.of(
                    new RoadmapSubject(1, "General Aptitude",
                        "Understand the aptitude portion and its question types."),
                    new RoadmapSubject(2, "Engineering Mathematics",
                        "Review the mathematics syllabus and its major areas.")
                ),
                List.of(
                    "Understand the exam pattern and marking scheme.",
                    "Review the official syllabus for GATE CSE.",
                    "Create a realistic preparation schedule."
                ),
                "Understand the exam and set your direction.",
                "/syllabus",
                "Explore Syllabus"
            ),

            new RoadmapPhase(
                "02",
                "Build Mathematical Foundations",
                "Strengthen mathematical and digital foundations before moving deeper into core computer science.",
                List.of(
                    new RoadmapSubject(1, "Discrete Mathematics",
                        "Logic, sets, relations, combinatorics and graph theory."),
                    new RoadmapSubject(2, "Digital Logic",
                        "Boolean algebra, circuits and sequential logic."),
                    new RoadmapSubject(3, "Computer Organization and Architecture",
                        "Processor basics, memory and computer architecture.")
                ),
                List.of(
                    "Organize these subjects into a study sequence.",
                    "Complete topic-wise practice as you progress.",
                    "Revisit concepts that you find difficult."
                ),
                "Build a strong conceptual foundation.",
                "/syllabus",
                "View Syllabus"
            ),

            new RoadmapPhase(
                "03",
                "Programming and Algorithms",
                "Develop the programming and problem-solving foundations used throughout computer science.",
                List.of(
                    new RoadmapSubject(1, "Programming and Data Structures",
                        "Programming concepts, stacks, queues, trees and graphs."),
                    new RoadmapSubject(2, "Algorithms",
                        "Searching, sorting, algorithm analysis and design techniques.")
                ),
                List.of(
                    "Review essential data structures and their operations.",
                    "Practise algorithmic problem-solving.",
                    "Solve questions and analyse incorrect answers."
                ),
                "Develop problem-solving confidence.",
                "/practice",
                "Explore Practice"
            ),

            new RoadmapPhase(
                "04",
                "Core Computer Science Subjects",
                "Work through the major systems and theory subjects in a logical sequence.",
                List.of(
                    new RoadmapSubject(1, "Theory of Computation",
                        "Automata, formal languages and computability."),
                    new RoadmapSubject(2, "Compiler Design",
                        "Lexical analysis, parsing and translation."),
                    new RoadmapSubject(3, "Operating Systems",
                        "Processes, scheduling, memory and synchronization."),
                    new RoadmapSubject(4, "Database Management Systems",
                        "Relational models, SQL, normalization and transactions."),
                    new RoadmapSubject(5, "Computer Networks",
                        "Networking layers, protocols and communication.")
                ),
                List.of(
                    "Follow the subject sequence and cover syllabus topics.",
                    "Practise topic-wise questions after each subject area.",
                    "Record difficult concepts for later revision."
                ),
                "Build coverage across the core syllabus.",
                "/syllabus",
                "Review Syllabus"
            ),

            new RoadmapPhase(
                "05",
                "Previous-Year Questions and Revision",
                "Use previous-year questions to assess your understanding and identify gaps in preparation.",
                List.of(
                    new RoadmapSubject(1, "Subject-wise PYQs",
                        "Apply concepts to questions from previous GATE papers."),
                    new RoadmapSubject(2, "Weak-topic Revision",
                        "Revisit areas where accuracy or understanding needs improvement.")
                ),
                List.of(
                    "Solve PYQs topic by topic.",
                    "Review mistakes and understand why answers were incorrect.",
                    "Revisit weak topics and practise them again."
                ),
                "Improve accuracy and question-solving skills.",
                "/pyqs",
                "Explore PYQs"
            ),

            new RoadmapPhase(
                "06",
                "Mock Tests and Exam Readiness",
                "Bring your preparation together with timed tests, analysis and final revision.",
                List.of(
                    new RoadmapSubject(1, "Full-length Mock Tests",
                        "Practise the exam experience under timed conditions."),
                    new RoadmapSubject(2, "Performance Analysis",
                        "Review accuracy, time management and weak areas."),
                    new RoadmapSubject(3, "Final Revision",
                        "Revise important concepts and previously difficult questions.")
                ),
                List.of(
                    "Attempt full-length mock tests regularly.",
                    "Analyse every test and identify improvement areas.",
                    "Plan final revision based on your mistakes."
                ),
                "Be ready to approach the actual exam confidently.",
                "/mock-tests",
                "Take Mock Tests"
            )
        );

        model.addAttribute("selectedPaper", selectedPaper);
        model.addAttribute("paperName",
            "Computer Science and Information Technology");
        model.addAttribute("paperOptions", paperOptions);
        model.addAttribute("phases", phases);

        return "roadmap";
    }

    public record PaperOption(
        String code,
        String name,
        boolean available
    ) {}

    public record RoadmapSubject(
        int order,
        String name,
        String focus
    ) {}

    public record RoadmapPhase(
        String number,
        String title,
        String description,
        List<RoadmapSubject> subjects,
        List<String> milestones,
        String goal,
        String actionUrl,
        String actionLabel
    ) {}
}
