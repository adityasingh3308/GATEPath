package com.example.GATEPath.Repository;

import com.example.GATEPath.Entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository
        extends JpaRepository<Question, String> {

    List<Question> findByTopicIgnoreCaseOrderByQuestionIdAsc(
            String topic
    );

    List<Question> findByTopicIgnoreCaseAndSubtopicIgnoreCaseOrderByQuestionIdAsc(
            String topic,
            String subtopic
    );
}