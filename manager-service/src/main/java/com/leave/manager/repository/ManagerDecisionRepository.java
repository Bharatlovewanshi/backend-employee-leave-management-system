package com.leave.manager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.leave.manager.entity.ManagerDecision;
import com.leave.manager.enums.DecisionType;

/**
 * Repository interface for managing ManagerDecision entities.
 * Provides database operations and custom query methods
 * for manager leave approval and rejection decisions.
 */
@Repository
public interface ManagerDecisionRepository extends JpaRepository<ManagerDecision, Long> {

    /**
     * Retrieves all leave decisions made by a specific manager.
     *
     * @param managerId unique ID of the manager
     * @return list of decisions made by the manager
     */
    List<ManagerDecision> findByManagerId(Long managerId);

    /**
     * Retrieves the decision associated with a specific leave request.
     *
     * @param leaveRequestId unique ID of the leave request
     * @return optional containing the decision if found
     */
    Optional<ManagerDecision> findByLeaveRequestId(Long leaveRequestId);

    /**
     * Retrieves all leave decisions based on their decision type.
     *
     * @param decisionType type of decision (APPROVED or REJECTED)
     * @return list of matching decisions
     */
    List<ManagerDecision> findByDecisionType(DecisionType decisionType);

    /**
     * Retrieves decisions made by a specific manager with a given decision type.
     *
     * @param managerId unique ID of the manager
     * @param decisionType type of decision
     * @return list of matching decisions
     */
    List<ManagerDecision> findByManagerIdAndDecisionType(
            Long managerId, DecisionType decisionType);
}