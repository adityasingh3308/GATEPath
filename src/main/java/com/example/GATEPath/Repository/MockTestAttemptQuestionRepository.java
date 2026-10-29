
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.MockTestAttemptQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MockTestAttemptQuestionRepository
        extends JpaRepository<MockTestAttemptQuestion, Long> {

    List<MockTestAttemptQuestion> findByAttempt_IdOrderByQuestionOrderAsc(
            Long attemptId
    );

    Optional<MockTestAttemptQuestion> findByAttempt_IdAndQuestionOrder(
            Long attemptId,
            Integer questionOrder
    );

    Optional<MockTestAttemptQuestion> findByAttempt_IdAndQuestionId(
            Long attemptId,
            String questionId
    );
}
