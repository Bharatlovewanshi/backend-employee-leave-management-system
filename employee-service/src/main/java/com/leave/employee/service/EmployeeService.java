
package com.leave.employee.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.leave.employee.dto.request.EmployeeUpdateRequest;
import com.leave.employee.dto.response.EmployeeResponse;
import com.leave.employee.entity.Employee;
import com.leave.employee.entity.User;
import com.leave.employee.repository.EmployeeRepository;
import com.leave.employee.repository.UserRepository;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository) {

        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
    }

    // Save employee
    public Employee saveEmployee(Employee employee) {
        return employeeRepository.save(employee);
    }

    // Get employee by ID
    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id);
    }

    // Get employee by email
    public Optional<Employee> getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email);
    }

    // Get employee by employee code
    public Optional<Employee> getEmployeeByCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode);
    }

    // Get employee by user ID
    public Optional<Employee> getEmployeeByUserId(Long userId) {
        return employeeRepository.findByUserId(userId);
    }

    // Get all employees
    public List<EmployeeResponse> getAllEmployees() {

        List<Employee> employees = employeeRepository.findAll();

        return employees.stream()
                .map(employee -> new EmployeeResponse(
                        employee.getId(),
                        employee.getUser().getId(),
                        employee.getEmployeeCode(),
                        employee.getEmail(),
                        employee.getFirstName(),
                        employee.getLastName(),
                        employee.getDepartment(),
                        employee.getDesignation(),
                        employee.getUser().getRole().name()
                ))
                .collect(Collectors.toList());
    }

    // Get employee by ID as response DTO
    public EmployeeResponse getEmployeeResponseById(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found with ID: " + id
                ));

        return new EmployeeResponse(
                employee.getId(),
                employee.getUser().getId(),
                employee.getEmployeeCode(),
                employee.getEmail(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getDepartment(),
                employee.getDesignation(),
                employee.getUser().getRole().name()
        );
    }
    

		public EmployeeResponse getEmployeeResponseByEmail(String email) {
		
		    Employee employee = employeeRepository.findByEmail(email)
		            .orElseThrow(() -> new ResponseStatusException(
		                    HttpStatus.NOT_FOUND,
		                    "Employee profile not found"
		            ));
		
		    return new EmployeeResponse(
		            employee.getId(),
		            employee.getUser().getId(),
		            employee.getEmployeeCode(),
		            employee.getEmail(),
		            employee.getFirstName(),
		            employee.getLastName(),
		            employee.getDepartment(),
		            employee.getDesignation(),
		            employee.getUser().getRole().name()
		    );
		}


    // Update employee
    @Transactional
    public EmployeeResponse updateEmployee(
            Long id, EmployeeUpdateRequest request) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found with ID: " + id
                ));

        // Check whether email belongs to another employee
        if (!employee.getEmail().equalsIgnoreCase(request.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        // Update employee details
        employee.setEmail(request.getEmail());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());

        // Keep linked User email synchronized
        employee.getUser().setEmail(request.getEmail());

        Employee updatedEmployee = employeeRepository.save(employee);

        return new EmployeeResponse(
                updatedEmployee.getId(),
                updatedEmployee.getUser().getId(),
                updatedEmployee.getEmployeeCode(),
                updatedEmployee.getEmail(),
                updatedEmployee.getFirstName(),
                updatedEmployee.getLastName(),
                updatedEmployee.getDepartment(),
                updatedEmployee.getDesignation(),
                updatedEmployee.getUser().getRole().name()
        );
    }

    // Delete employee and linked user
    @Transactional
    public void deleteEmployee(Long id) {

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Employee not found with ID: " + id
                ));

        User user = employee.getUser();

        // Delete employee first
        employeeRepository.delete(employee);
        employeeRepository.flush();

        // Delete linked user
        userRepository.delete(user);
    }

		@Transactional
		public EmployeeResponse updateMyProfile(
		        String email,
		        EmployeeUpdateRequest request) {
		
		    Employee employee = employeeRepository.findByEmail(email)
		            .orElseThrow(() -> new ResponseStatusException(
		                    HttpStatus.NOT_FOUND,
		                    "Employee profile not found"
		            ));
		
		    employee.setFirstName(request.getFirstName());
		    employee.setLastName(request.getLastName());
		    employee.setDepartment(request.getDepartment());
		    employee.setDesignation(request.getDesignation());
		
		    Employee updatedEmployee = employeeRepository.save(employee);
		
		    return new EmployeeResponse(
		            updatedEmployee.getId(),
		            updatedEmployee.getUser().getId(),
		            updatedEmployee.getEmployeeCode(),
		            updatedEmployee.getEmail(),
		            updatedEmployee.getFirstName(),
		            updatedEmployee.getLastName(),
		            updatedEmployee.getDepartment(),
		            updatedEmployee.getDesignation(),
		            updatedEmployee.getUser().getRole().name()
		    );
		}

}
