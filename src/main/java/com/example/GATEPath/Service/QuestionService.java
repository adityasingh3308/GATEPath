package com.example.GATEPath.Service;

import com.example.GATEPath.Entity.Question;
import com.example.GATEPath.Repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getQuestionsByTopic(String topic) {
        return questionRepository
                .findByTopicIgnoreCaseOrderByQuestionIdAsc(topic);
    }

    public List<String> getAvailableSubjects() {
        return getAllQuestions().stream()
                .map(Question::getTopic)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> getAvailableSubtopics(String subject) {
        if (subject == null || subject.isBlank()) {
            return List.of();
        }

        return getQuestionsByTopic(subject).stream()
                .map(Question::getSubtopic)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<Question> getPracticeQuestions(
            String subject,
            String subtopic,
            String questionType,
            int limit) {

        List<Question> questions;

        if (subject == null || subject.isBlank()) {
            questions = getAllQuestions();
        } else if (subtopic != null
                && !subtopic.isBlank()
                && !"ALL".equalsIgnoreCase(subtopic)) {

            questions = questionRepository
                    .findByTopicIgnoreCaseAndSubtopicIgnoreCaseOrderByQuestionIdAsc(
                            subject, subtopic);
        } else {
            questions = getQuestionsByTopic(subject);
        }

        if (questionType != null
                && !questionType.isBlank()
                && !"ALL".equalsIgnoreCase(questionType)) {

            String selectedType = questionType
                    .trim()
                    .toUpperCase(Locale.ROOT);

            questions = questions.stream()
                    .filter(question ->
                            question.getQuestionType() != null
                            && selectedType.equals(
                                    question.getQuestionType()
                                            .trim()
                                            .toUpperCase(Locale.ROOT)))
                    .toList();
        }

        if (limit > 0 && questions.size() > limit) {
            questions = questions.subList(0, limit);
        }

        return questions;
    }
}