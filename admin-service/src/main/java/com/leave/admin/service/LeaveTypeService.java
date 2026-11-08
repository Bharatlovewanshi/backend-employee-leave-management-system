package com.leave.admin.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.leave.admin.entity.LeaveType;
import com.leave.admin.repository.LeaveTypeRepository;

@Service
public class LeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    public LeaveTypeService(LeaveTypeRepository leaveTypeRepository) {
        this.leaveTypeRepository = leaveTypeRepository;
    }

    // Get all leave types
    public List<LeaveType> getAllLeaveTypes() {
        return leaveTypeRepository.findAll();
    }

    // Add a new leave type
    public LeaveType addLeaveType(LeaveType leaveType) {

        if (leaveTypeRepository.existsByLeaveTypeName(
                leaveType.getLeaveTypeName())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Leave type already exists");
        }

        return leaveTypeRepository.save(leaveType);
    }

    // Update an existing leave type
    public LeaveType updateLeaveType(
            Long leaveTypeId, LeaveType updatedLeaveType) {

        LeaveType existingLeaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Leave type not found"));

        if (leaveTypeRepository.existsByLeaveTypeNameAndLeaveTypeIdNot(
                updatedLeaveType.getLeaveTypeName(), leaveTypeId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Another leave type has this name");
        }

        existingLeaveType.setLeaveTypeName(
                updatedLeaveType.getLeaveTypeName());
        existingLeaveType.setDescription(
                updatedLeaveType.getDescription());
        existingLeaveType.setAnnualQuota(
                updatedLeaveType.getAnnualQuota());
        existingLeaveType.setAllowNegativeBalance(
                updatedLeaveType.getAllowNegativeBalance());

        return leaveTypeRepository.save(existingLeaveType);
    }

    // Delete a leave type
    public void deleteLeaveType(Long leaveTypeId) {

        if (!leaveTypeRepository.existsById(leaveTypeId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Leave type not found");
        }

        leaveTypeRepository.deleteById(leaveTypeId);
    }
}