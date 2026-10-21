
package com.example.GATEPath.Controller;

import com.example.GATEPath.Entity.SyllabusSubject;
import com.example.GATEPath.Repository.SyllabusSubjectRepository;
import com.example.GATEPath.Repository.SyllabusTopicRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/syllabus")
public class SyllabusController {

    private final SyllabusSubjectRepository subjectRepository;
    private final SyllabusTopicRepository topicRepository;

    public SyllabusController(
            SyllabusSubjectRepository subjectRepository,
            SyllabusTopicRepository topicRepository) {

        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
    }

    // Display all CS subjects
    @GetMapping
    public String showSyllabus(Model model) {

        model.addAttribute(
                "subjects",
                subjectRepository
                        .findByBranchOrderByDisplayOrderAsc("CS")
        );

        return "syllabus/syllabus";
    }

    // Display topics for a selected subject
    @GetMapping("/{id}")
    public String showSubjectDetails(
            @PathVariable Long id,
            Model model) {

        SyllabusSubject subject = subjectRepository.findById(id)
                .filter(s -> "CS".equals(s.getBranch()))
                .orElse(null);

        if (subject == null) {
            return "redirect:/syllabus";
        }

        model.addAttribute("subject", subject);

        model.addAttribute(
                "topics",
                topicRepository
                        .findBySubject_IdOrderByDisplayOrderAsc(id)
        );

        return "syllabus/subject-details";
    }
}
