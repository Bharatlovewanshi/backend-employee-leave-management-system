
package com.leave.employee.controller;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.leave.employee.dto.request.LoginRequest;
import com.leave.employee.dto.request.RegisterRequest;
import com.leave.employee.dto.request.CreateUserRequest;

import com.leave.employee.dto.response.LoginResponse;
import com.leave.employee.dto.response.RegisterResponse;

import com.leave.employee.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Employee registration
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerEmployee(
            @Valid @RequestBody RegisterRequest request) {

        RegisterResponse response =
                authService.registerEmployee(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // User login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        return ResponseEntity.ok(response);
    }

    // Admin creates Employee or Manager account
    @PostMapping("/users")
    public ResponseEntity<RegisterResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        RegisterResponse response = authService.createUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
