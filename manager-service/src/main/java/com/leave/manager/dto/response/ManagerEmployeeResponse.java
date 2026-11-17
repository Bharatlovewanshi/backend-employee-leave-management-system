
package com.leave.manager.dto.response;

public class ManagerEmployeeResponse {

    private Long employeeUserId;
    private Long employeeId;
    private String employeeName;
    private String email;
    private String designation;

    public ManagerEmployeeResponse() {
    }

    public ManagerEmployeeResponse(
            Long employeeUserId,
            Long employeeId,
            String employeeName,
            String email,
            String designation) {
        this.employeeUserId = employeeUserId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.email = email;
        this.designation = designation;
    }

    public Long getEmployeeUserId() {
        return employeeUserId;
    }

    public void setEmployeeUserId(Long employeeUserId) {
        this.employeeUserId = employeeUserId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
