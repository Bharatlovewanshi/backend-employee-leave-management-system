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

    @Column(nullable = false)
    private Long leaveRequestId;

    @Column(nullable = false)
    private Long managerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionType decisionType;

    @Column(length = 500)
    private String comments;

    @Column(nullable = false, updatable = false)
    private LocalDateTime decisionDate;

    @PrePersist
    protected void onCreate() {
        this.decisionDate = LocalDateTime.now();
    }
}