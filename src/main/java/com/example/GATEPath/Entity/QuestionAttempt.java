
package com.example.GATEPath.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "question_attempts",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_user_question",
            columnNames = {"user_id", "question_id"}
        )
    }
)
public class QuestionAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "question_id", nullable = false, length = 100)
    private String questionId;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ASSIGNED";

    public QuestionAttempt() {
    }

    @PrePersist
    public void prePersist() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
