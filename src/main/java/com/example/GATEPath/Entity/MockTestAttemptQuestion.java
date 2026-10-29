
package com.example.GATEPath.Entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "mock_test_attempt_questions",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_attempt_question",
            columnNames = {"attempt_id", "question_id"}
        ),
        @UniqueConstraint(
            name = "uk_attempt_question_order",
            columnNames = {"attempt_id", "question_order"}
        )
    }
)
public class MockTestAttemptQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_question_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attempt_id", nullable = false)
    private MockTestAttempt attempt;

    @Column(name = "question_id", nullable = false, length = 255)
    private String questionId;

    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    @Column(nullable = false)
    private Double marks = 1.0;

    @Column(name = "negative_marks", nullable = false)
    private Double negativeMarks = 0.0;

    @Column(name = "selected_answer", columnDefinition = "TEXT")
    private String selectedAnswer;

    @Column(name = "marked_for_review", nullable = false)
    private Boolean markedForReview = false;

    @Column(nullable = false)
    private Boolean visited = false;

    public MockTestAttemptQuestion() {
    }

    public Long getId() {
        return id;
    }

    public MockTestAttempt getAttempt() {
        return attempt;
    }

    public void setAttempt(MockTestAttempt attempt) {
        this.attempt = attempt;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionOrder() {
        return questionOrder;
    }

    public void setQuestionOrder(Integer questionOrder) {
        this.questionOrder = questionOrder;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    public Double getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(Double negativeMarks) {
        this.negativeMarks = negativeMarks;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public void setSelectedAnswer(String selectedAnswer) {
        this.selectedAnswer = selectedAnswer;
    }

    public Boolean getMarkedForReview() {
        return markedForReview;
    }

    public void setMarkedForReview(Boolean markedForReview) {
        this.markedForReview = markedForReview;
    }

    public Boolean getVisited() {
        return visited;
    }

    public void setVisited(Boolean visited) {
        this.visited = visited;
    }
}
