
package com.leave.employee.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.leave.employee.dto.request.LoginRequest;
import com.leave.employee.dto.request.RegisterRequest;
import com.leave.employee.dto.request.CreateUserRequest;
import com.leave.employee.dto.response.LoginResponse;
import com.leave.employee.dto.response.RegisterResponse;
import com.leave.employee.entity.Employee;
import com.leave.employee.entity.User;
import com.leave.employee.enums.UserRole;
import com.leave.employee.repository.EmployeeRepository;
import com.leave.employee.repository.UserRepository;
import com.leave.employee.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Employee registration
    @Transactional
    public RegisterResponse registerEmployee(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Employee code already exists"
            );
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.EMPLOYEE);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        Employee employee = new Employee();
        employee.setUser(savedUser);
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setDepartment(request.getDepartment());
        employee.setDesignation(request.getDesignation());

        Employee savedEmployee = employeeRepository.save(employee);

        return new RegisterResponse(
                savedUser.getId(),
                savedEmployee.getId(),
                savedUser.getEmail(),
                savedEmployee.getFirstName(),
                savedEmployee.getLastName(),
                savedUser.getRole().name(),
                "Employee registered successfully"
        );
    }

    // Employee login
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is disabled"
            );
        }

        // Temporary password debugging
        boolean passwordMatches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        System.out.println("Login email: " + user.getEmail());
        System.out.println("Password matches: " + passwordMatches);

        if (!passwordMatches) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                "Login successful"
        );
    }

    // Create Manager or Employee account
    @Transactional
    public RegisterResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        // Prevent ADMIN account creation through this API
        if (request.getRole() == UserRole.ADMIN) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "ADMIN account creation is not allowed through this API"
            );
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);

        // Create Employee profile only for EMPLOYEE role
        if (request.getRole() == UserRole.EMPLOYEE) {

            if (request.getFirstName() == null || request.getFirstName().isBlank()
                    || request.getLastName() == null || request.getLastName().isBlank()
                    || request.getEmployeeCode() == null || request.getEmployeeCode().isBlank()
                    || request.getDepartment() == null || request.getDepartment().isBlank()
                    || request.getDesignation() == null || request.getDesignation().isBlank()) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "All employee profile fields are required"
                );
            }

            if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Employee code already exists"
                );
            }

            Employee employee = new Employee();
            employee.setUser(savedUser);
            employee.setEmployeeCode(request.getEmployeeCode());
            employee.setEmail(request.getEmail());
            employee.setFirstName(request.getFirstName());
            employee.setLastName(request.getLastName());
            employee.setDepartment(request.getDepartment());
            employee.setDesignation(request.getDesignation());

            Employee savedEmployee = employeeRepository.save(employee);

            return new RegisterResponse(
                    savedUser.getId(),
                    savedEmployee.getId(),
                    savedUser.getEmail(),
                    savedEmployee.getFirstName(),
                    savedEmployee.getLastName(),
                    savedUser.getRole().name(),
                    "Employee account created successfully"
            );
        }

        // Manager account does not require Employee profile
        return new RegisterResponse(
                savedUser.getId(),
                null,
                savedUser.getEmail(),
                null,
                null,
                savedUser.getRole().name(),
                "Manager account created successfully"
        );
    }
}
