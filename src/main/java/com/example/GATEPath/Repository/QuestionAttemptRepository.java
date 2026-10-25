
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.QuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionAttemptRepository
        extends JpaRepository<QuestionAttempt, Long> {

    // All questions already assigned to this user
    List<QuestionAttempt> findByUserId(Long userId);

    // Check whether this user has already received this question
    boolean existsByUserIdAndQuestionId(
            Long userId,
            String questionId
    );
}
