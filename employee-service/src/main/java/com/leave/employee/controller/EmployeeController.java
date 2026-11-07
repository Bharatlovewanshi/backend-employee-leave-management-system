package com.leave.employee.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.leave.employee.dto.request.EmployeeUpdateRequest;
import com.leave.employee.dto.response.EmployeeResponse;
import com.leave.employee.service.EmployeeService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // Get all employees
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(employees);
    }

    // Get employee by ID
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployeeById(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployeeResponseById(id);
        return ResponseEntity.ok(employee);
    }

    // Update employee
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {

        EmployeeResponse updatedEmployee =
                employeeService.updateEmployee(id, request);

        return ResponseEntity.ok(updatedEmployee);
    }

    // Delete employee and linked user
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployee(@PathVariable Long id) {

        employeeService.deleteEmployee(id);

        return ResponseEntity.ok(
                "Employee and linked user deleted successfully"
        );
    }

	@GetMapping("/me")
	public ResponseEntity<EmployeeResponse> getMyProfile(
	        Authentication authentication) {
	
	    String email = authentication.getName();
	
	    EmployeeResponse employee =
	            employeeService.getEmployeeResponseByEmail(email);
	
	    return ResponseEntity.ok(employee);
	}

	@PutMapping("/me")
	public ResponseEntity<EmployeeResponse> updateMyProfile(
	        Authentication authentication,
	        @Valid @RequestBody EmployeeUpdateRequest request) {
	
	    String email = authentication.getName();
	
	    EmployeeResponse updatedEmployee =
	            employeeService.updateMyProfile(email, request);
	
	    return ResponseEntity.ok(updatedEmployee);
}

}