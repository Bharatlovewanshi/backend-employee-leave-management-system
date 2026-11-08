
package com.leave.admin.dto;

public class ManagerResponse {

    private Long managerId;
    private String managerName;
    private String email;
    private String designation;

    public ManagerResponse() {
    }

    public ManagerResponse(
            Long managerId,
            String managerName,
            String email,
            String designation) {

        this.managerId = managerId;
        this.managerName = managerName;
        this.email = email;
        this.designation = designation;
    }

    public Long getManagerId() {
        return managerId;
    }

    public void setManagerId(Long managerId) {
        this.managerId = managerId;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
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
