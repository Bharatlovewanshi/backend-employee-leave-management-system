
package com.leave.admin.dto;

public class AdminDashboardResponse {

    private long totalEmployees;
    private long totalManagers;
    private long totalLeaveRequests;
    private long approvedLeaves;
    private long pendingLeaves;
    private long rejectedLeaves;
    private long cancelledLeaves;

    public AdminDashboardResponse() {
    }

    public AdminDashboardResponse(
            long totalEmployees,
            long totalManagers,
            long totalLeaveRequests,
            long approvedLeaves,
            long pendingLeaves,
            long rejectedLeaves,
            long cancelledLeaves) {

        this.totalEmployees = totalEmployees;
        this.totalManagers = totalManagers;
        this.totalLeaveRequests = totalLeaveRequests;
        this.approvedLeaves = approvedLeaves;
        this.pendingLeaves = pendingLeaves;
        this.rejectedLeaves = rejectedLeaves;
        this.cancelledLeaves = cancelledLeaves;
    }

    public long getTotalEmployees() {
        return totalEmployees;
    }

    public void setTotalEmployees(long totalEmployees) {
        this.totalEmployees = totalEmployees;
    }

    public long getTotalManagers() {
        return totalManagers;
    }

    public void setTotalManagers(long totalManagers) {
        this.totalManagers = totalManagers;
    }

    public long getTotalLeaveRequests() {
        return totalLeaveRequests;
    }

    public void setTotalLeaveRequests(long totalLeaveRequests) {
        this.totalLeaveRequests = totalLeaveRequests;
    }

    public long getApprovedLeaves() {
        return approvedLeaves;
    }

    public void setApprovedLeaves(long approvedLeaves) {
        this.approvedLeaves = approvedLeaves;
    }

    public long getPendingLeaves() {
        return pendingLeaves;
    }

    public void setPendingLeaves(long pendingLeaves) {
        this.pendingLeaves = pendingLeaves;
    }

    public long getRejectedLeaves() {
        return rejectedLeaves;
    }

    public void setRejectedLeaves(long rejectedLeaves) {
        this.rejectedLeaves = rejectedLeaves;
    }

    public long getCancelledLeaves() {
        return cancelledLeaves;
    }

    public void setCancelledLeaves(long cancelledLeaves) {
        this.cancelledLeaves = cancelledLeaves;
    }
}
