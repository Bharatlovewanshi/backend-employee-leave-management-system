package com.leave.admin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "leave_types")
public class LeaveType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leaveTypeId;

    @Column(nullable = false, unique = true)
    private String leaveTypeName;

    private String description;

    private Integer annualQuota;

    private Boolean allowNegativeBalance = false;

    public LeaveType() {
    }

    public Long getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(Long leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public String getLeaveTypeName() {
        return leaveTypeName;
    }

    public void setLeaveTypeName(String leaveTypeName) {
        this.leaveTypeName = leaveTypeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getAnnualQuota() {
        return annualQuota;
    }

    public void setAnnualQuota(Integer annualQuota) {
        this.annualQuota = annualQuota;
    }

    public Boolean getAllowNegativeBalance() {
        return allowNegativeBalance;
    }

    public void setAllowNegativeBalance(Boolean allowNegativeBalance) {
        this.allowNegativeBalance = allowNegativeBalance;
    }
}