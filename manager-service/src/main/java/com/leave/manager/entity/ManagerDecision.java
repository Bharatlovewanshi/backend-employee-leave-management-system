package com.leave.manager.entity;

import java.time.LocalDateTime;

import com.leave.manager.enums.DecisionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "manager_decisions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "leave_request_id", nullable = false, unique = true)
    private Long leaveRequestId;

    @Column(name = "manager_id", nullable = false)
    private Long managerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 20)
    private DecisionType decision;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "decision_at", nullable = false, updatable = false)
    private LocalDateTime decisionAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        this.decisionAt = now;
        this.createdAt = now;
    }

}
