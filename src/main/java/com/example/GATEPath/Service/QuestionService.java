
package com.example.GATEPath.Service;

import com.example.GATEPath.Entity.Question;
import com.example.GATEPath.Entity.QuestionAttempt;
import com.example.GATEPath.Repository.QuestionAttemptRepository;
import com.example.GATEPath.Repository.QuestionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuestionAttemptRepository questionAttemptRepository;

    public QuestionService(
            QuestionRepository questionRepository,
            QuestionAttemptRepository questionAttemptRepository) {
        this.questionRepository = questionRepository;
        this.questionAttemptRepository = questionAttemptRepository;
    }

    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    public List<Question> getQuestionsByTopic(String topic) {
        if (topic == null || topic.isBlank()) {
            return List.of();
        }

        return questionRepository
                .findByTopicIgnoreCaseOrderByQuestionIdAsc(topic.trim());
    }

    public List<String> getAvailableSubjects() {
        return getAllQuestions().stream()
                .map(Question::getSubject)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> getAvailableTopics(String subject) {
        if (subject == null || subject.isBlank()) {
            return List.of();
        }

        return questionRepository
                .findBySubjectIgnoreCaseOrderByQuestionIdAsc(subject.trim())
                .stream()
                .map(Question::getTopic)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> getAvailableSubtopics(String topic) {
        if (topic == null || topic.isBlank()) {
            return List.of();
        }

        return getQuestionsByTopic(topic).stream()
                .map(Question::getSubtopic)
                .filter(value -> value != null && !value.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    // Existing method retained for compatibility with other pages.
    public List<Question> getPracticeQuestions(
            String subject,
            String topic,
            String questionType,
            int limit) {

        List<Question> questions =
                getFilteredQuestions(subject, topic, questionType);

        Collections.shuffle(questions);

        if (limit > 0 && questions.size() > limit) {
            return new ArrayList<>(questions.subList(0, limit));
        }

        return questions;
    }

    // New method: never assign the same question to the same user twice.
    @Transactional
    public List<Question> getPracticeQuestionsForUser(
            Long userId,
            String subject,
            String topic,
            String questionType,
            int limit) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "A logged-in user ID is required.");
        }

        List<QuestionAttempt> previousAttempts =
                questionAttemptRepository.findByUserId(userId);

        Set<String> previouslyAssignedIds = new HashSet<>();

        for (QuestionAttempt attempt : previousAttempts) {
            previouslyAssignedIds.add(attempt.getQuestionId());
        }

        List<Question> availableQuestions =
                getFilteredQuestions(subject, topic, questionType);

        // Remove questions already assigned to this particular user.
        availableQuestions.removeIf(question ->
                previouslyAssignedIds.contains(question.getQuestionId()));

        Collections.shuffle(availableQuestions);

        if (limit > 0 && availableQuestions.size() > limit) {
            availableQuestions = new ArrayList<>(
                    availableQuestions.subList(0, limit));
        }

        // Record assignment before returning questions to the controller.
        List<QuestionAttempt> newAttempts = new ArrayList<>();

        for (Question question : availableQuestions) {
            QuestionAttempt attempt = new QuestionAttempt();
            attempt.setUserId(userId);
            attempt.setQuestionId(question.getQuestionId());
            attempt.setStatus("ASSIGNED");
            newAttempts.add(attempt);
        }

        if (!newAttempts.isEmpty()) {
            questionAttemptRepository.saveAll(newAttempts);
        }

        return availableQuestions;
    }

    private List<Question> getFilteredQuestions(
            String subject,
            String topic,
            String questionType) {

        if (subject == null || subject.isBlank()) {
            return new ArrayList<>();
        }

        List<Question> questions;

        if (topic != null
                && !topic.isBlank()
                && !"ALL".equalsIgnoreCase(topic.trim())) {

            questions = new ArrayList<>(
                    questionRepository
                            .findBySubjectIgnoreCaseAndTopicIgnoreCaseOrderByQuestionIdAsc(
                                    subject.trim(), topic.trim()));
        } else {
            questions = new ArrayList<>(
                    questionRepository
                            .findBySubjectIgnoreCaseOrderByQuestionIdAsc(
                                    subject.trim()));
        }

        if (questionType != null
                && !questionType.isBlank()
                && !"ALL".equalsIgnoreCase(questionType.trim())) {

            String selectedType =
                    questionType.trim().toUpperCase(Locale.ROOT);

            questions.removeIf(question ->
                    question.getQuestionType() == null
                    || !selectedType.equals(
                            question.getQuestionType()
                                    .trim()
                                    .toUpperCase(Locale.ROOT)));
        }

        return questions;
    }
}
