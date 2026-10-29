
package com.example.GATEPath.Service;

import com.example.GATEPath.Entity.MockTest;
import com.example.GATEPath.Entity.MockTestAttempt;
import com.example.GATEPath.Entity.MockTestAttemptQuestion;
import com.example.GATEPath.Entity.MockTestQuestionPool;
import com.example.GATEPath.Entity.Question;
import com.example.GATEPath.Entity.User;

import com.example.GATEPath.Repository.MockTestRepository;
import com.example.GATEPath.Repository.MockTestQuestionPoolRepository;
import com.example.GATEPath.Repository.MockTestAttemptRepository;
import com.example.GATEPath.Repository.MockTestAttemptQuestionRepository;
import com.example.GATEPath.Repository.QuestionRepository;
import com.example.GATEPath.Repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MockTestService {

    private static final int GENERAL_APTITUDE_QUESTIONS = 10;
    private static final int CORE_CSE_QUESTIONS = 55;

    private final MockTestRepository mockTestRepository;
    private final MockTestQuestionPoolRepository questionPoolRepository;
    private final MockTestAttemptRepository attemptRepository;
    private final MockTestAttemptQuestionRepository attemptQuestionRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;

    public MockTestService(
            MockTestRepository mockTestRepository,
            MockTestQuestionPoolRepository questionPoolRepository,
            MockTestAttemptRepository attemptRepository,
            MockTestAttemptQuestionRepository attemptQuestionRepository,
            QuestionRepository questionRepository,
            UserRepository userRepository
    ) {
        this.mockTestRepository = mockTestRepository;
        this.questionPoolRepository = questionPoolRepository;
        this.attemptRepository = attemptRepository;
        this.attemptQuestionRepository = attemptQuestionRepository;
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public MockTestAttempt startOrResumeMockTest(
            Long mockTestId,
            String userEmail
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found."));

        MockTest mockTest = mockTestRepository.findById(mockTestId)
                .filter(test -> Boolean.TRUE.equals(test.getActive()))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Mock test not found or inactive."
                        ));

        Optional<MockTestAttempt> existingAttempt =
                attemptRepository
                        .findByUser_IdAndMockTest_IdAndStatus(
                                user.getId(),
                                mockTestId,
                                "IN_PROGRESS"
                        );

        if (existingAttempt.isPresent()) {
            MockTestAttempt attempt = existingAttempt.get();

            if (attempt.getExpiresAt().isAfter(LocalDateTime.now())) {
                return attempt;
            }

            attempt.setStatus("EXPIRED");
            attempt.setSubmittedAt(LocalDateTime.now());
            attemptRepository.save(attempt);
        }

        long previousAttempts =
                attemptRepository.countByUser_IdAndMockTest_Id(
                        user.getId(),
                        mockTestId
                );

        if (previousAttempts >= mockTest.getMaxAttempts()) {
            throw new IllegalStateException(
                    "You have used all attempts for this mock test."
            );
        }

        List<MockTestQuestionPool> generalPool =
                questionPoolRepository
                        .findByMockTest_IdAndSectionAndActiveTrue(
                                mockTestId,
                                "GENERAL_APTITUDE"
                        );

        List<MockTestQuestionPool> corePool =
                questionPoolRepository
                        .findByMockTest_IdAndSectionAndActiveTrue(
                                mockTestId,
                                "CORE_CSE"
                        );

        List<MockTestQuestionPool> eligibleGeneral =
                findExistingQuestions(generalPool);

        List<MockTestQuestionPool> eligibleCore =
                findExistingQuestions(corePool);

        if (eligibleGeneral.size() < GENERAL_APTITUDE_QUESTIONS
                || eligibleCore.size() < CORE_CSE_QUESTIONS) {
            throw new IllegalStateException(
                    "Insufficient eligible questions. "
                    + "The pool must contain at least 10 General Aptitude "
                    + "and 55 Core CSE questions."
            );
        }

        Collections.shuffle(eligibleGeneral);
        Collections.shuffle(eligibleCore);

        List<MockTestQuestionPool> selectedQuestions =
                new ArrayList<>();

        selectedQuestions.addAll(
                eligibleGeneral.subList(0, GENERAL_APTITUDE_QUESTIONS)
        );

        selectedQuestions.addAll(
                eligibleCore.subList(0, CORE_CSE_QUESTIONS)
        );

        MockTestAttempt attempt = new MockTestAttempt();
        attempt.setUser(user);
        attempt.setMockTest(mockTest);
        attempt.setAttemptNumber((int) previousAttempts + 1);
        attempt.setStatus("IN_PROGRESS");
        attempt.setExpiresAt(
                LocalDateTime.now().plusMinutes(
                        mockTest.getDurationMinutes()
                )
        );

        attempt = attemptRepository.save(attempt);

        int order = 1;

        for (MockTestQuestionPool poolItem : selectedQuestions) {
            MockTestAttemptQuestion attemptQuestion =
                    new MockTestAttemptQuestion();

            attemptQuestion.setAttempt(attempt);
            attemptQuestion.setQuestionId(poolItem.getQuestionId());
            attemptQuestion.setQuestionOrder(order++);
            attemptQuestion.setMarks(poolItem.getMarks());
            attemptQuestion.setNegativeMarks(
                    poolItem.getNegativeMarks()
            );
            attemptQuestion.setMarkedForReview(false);
            attemptQuestion.setVisited(false);

            attemptQuestionRepository.save(attemptQuestion);
        }

        return attempt;
    }

    private List<MockTestQuestionPool> findExistingQuestions(
            List<MockTestQuestionPool> pool
    ) {
        if (pool.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> questionIds = pool.stream()
                .map(MockTestQuestionPool::getQuestionId)
                .distinct()
                .toList();

        Set<String> existingIds = questionRepository
                .findAllById(questionIds)
                .stream()
                .map(Question::getQuestionId)
                .collect(Collectors.toCollection(HashSet::new));

        return pool.stream()
                .filter(item -> existingIds.contains(item.getQuestionId()))
                .collect(Collectors.toMap(
                        MockTestQuestionPool::getQuestionId,
                        Function.identity(),
                        (first, duplicate) -> first
                ))
                .values()
                .stream()
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
