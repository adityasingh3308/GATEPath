
package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository
        extends JpaRepository<Question, String> {

    // Fetch questions by topic
    List<Question> findByTopicIgnoreCaseOrderByQuestionIdAsc(
            String topic
    );

    // Fetch questions by topic and subtopic
    List<Question>
    findByTopicIgnoreCaseAndSubtopicIgnoreCaseOrderByQuestionIdAsc(
            String topic,
            String subtopic
    );

    // Fetch questions by subject
    List<Question> findBySubjectIgnoreCaseOrderByQuestionIdAsc(
            String subject
    );

    // Fetch questions by subject and topic
    List<Question>
    findBySubjectIgnoreCaseAndTopicIgnoreCaseOrderByQuestionIdAsc(
            String subject,
            String topic
    );

    // Fetch questions by subject, topic and subtopic
    List<Question>
    findBySubjectIgnoreCaseAndTopicIgnoreCaseAndSubtopicIgnoreCaseOrderByQuestionIdAsc(
            String subject,
            String topic,
            String subtopic
    );

    // Fetch questions not previously attempted by a user
    List<Question>
    findBySubjectIgnoreCaseAndQuestionIdNotInOrderByQuestionIdAsc(
            String subject,
            List<String> attemptedQuestionIds
    );

    // Fetch questions by subject and topic, excluding attempted IDs
    List<Question>
    findBySubjectIgnoreCaseAndTopicIgnoreCaseAndQuestionIdNotInOrderByQuestionIdAsc(
            String subject,
            String topic,
            List<String> attemptedQuestionIds
    );
}
