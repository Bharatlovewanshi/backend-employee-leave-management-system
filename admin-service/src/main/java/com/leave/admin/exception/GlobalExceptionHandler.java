
package com.leave.admin.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle admin operation errors (HTTP 404)
    @ExceptionHandler(AdminOperationException.class)
    public ResponseEntity<Map<String, String>> handleAdminOperationException(
            AdminOperationException exception) {

        Map<String, String> response = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    // Handle conflict errors (HTTP 409)
    @ExceptionHandler(AdminConflictException.class)
    public ResponseEntity<Map<String, String>> handleAdminConflictException(
            AdminConflictException exception) {

        Map<String, String> response = Map.of(
                "error", exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(response);
    }

    // Handle request validation errors (HTTP 400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        Map<String, String> response = Map.of(
                "error", message
        );

        return ResponseEntity
                .badRequest()
                .body(response);
    }
}
