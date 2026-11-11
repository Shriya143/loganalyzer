package com.example.loganalyzer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_PROBLEM")
public class UserProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000, nullable = false)
    private String problemText;

    @Column(nullable = false)
    private String status; // SOLVED or UNSOLVED

    private Long solutionId;

    private LocalDateTime createdAt;

    public UserProblem() {}

    public UserProblem(String problemText, String status, Long solutionId) {
        this.problemText = problemText;
        this.status = status;
        this.solutionId = solutionId;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProblemText() {
        return problemText;
    }

    public void setProblemText(String problemText) {
        this.problemText = problemText;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getSolutionId() {
        return solutionId;
    }

    public void setSolutionId(Long solutionId) {
        this.solutionId = solutionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
