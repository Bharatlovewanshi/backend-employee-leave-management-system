
package com.leave.admin.dto;

public class EmployeeResponse {

    private Long employeeId;
    private String employeeName;
    private String email;
    private String designation;

    public EmployeeResponse() {
    }

    public EmployeeResponse(Long employeeId, String employeeName,
            String email, String designation) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.email = email;
        this.designation = designation;
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
