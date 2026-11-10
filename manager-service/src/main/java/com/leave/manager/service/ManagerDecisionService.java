package com.leave.manager.service;

import java.util.List;

import com.leave.manager.dto.request.ManagerDecisionRequest;
import com.leave.manager.dto.response.ManagerDecisionResponse;
import com.leave.manager.dto.response.PendingLeaveResponse;

public interface ManagerDecisionService {

    ManagerDecisionResponse processDecision(
            ManagerDecisionRequest request,
            String authorization
    );

    List<PendingLeaveResponse> getPendingLeaveRequests(
            String authorization
    );

    List<ManagerDecisionResponse> getManagerDecisions(
            String authorization
    );
}