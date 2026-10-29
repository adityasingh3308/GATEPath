
package com.example.GATEPath.Entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "mock_test_question_pool",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_mock_test_question",
            columnNames = {"mock_test_id", "question_id"}
        )
    }
)
public class MockTestQuestionPool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pool_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mock_test_id", nullable = false)
    private MockTest mockTest;

    @Column(name = "question_id", nullable = false, length = 255)
    private String questionId;

    @Column(nullable = false, length = 50)
    private String section;

    @Column(nullable = false)
    private Double marks = 1.0;

    @Column(name = "negative_marks", nullable = false)
    private Double negativeMarks = 0.0;

    @Column(name = "is_active", nullable = false)
    private Boolean active = true;

    public MockTestQuestionPool() {
    }

    public Long getId() {
        return id;
    }

    public MockTest getMockTest() {
        return mockTest;
    }

    public void setMockTest(MockTest mockTest) {
        this.mockTest = mockTest;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
