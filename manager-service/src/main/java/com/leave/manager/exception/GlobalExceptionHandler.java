package com.leave.manager.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.leave.manager.dto.response.ApiResponse;

/**
 * Global exception handler for Manager Service.
 * Handles application-wide exceptions and returns
 * standardized API error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles resource not found exceptions.
     *
     * @param exception resource not found exception
     * @return 404 response with error message
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException exception) {

        ApiResponse<Void> response = new ApiResponse<>(
                false,
                exception.getMessage(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles invalid leave decision exceptions.
     *
     * @param exception invalid decision exception
     * @return 400 response with error message
     */
    @ExceptionHandler(InvalidDecisionException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDecisionException(
            InvalidDecisionException exception) {

        ApiResponse<Void> response = new ApiResponse<>(
                false,
                exception.getMessage(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles communication failures with other microservices.
     *
     * @param exception service communication exception
     * @return 503 response with error message
     */
    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceCommunicationException(
            ServiceCommunicationException exception) {

        ApiResponse<Void> response = new ApiResponse<>(
                false,
                exception.getMessage(),
                null
        );

        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handles request validation errors.
     *
     * @param exception validation exception
     * @return 400 response with validation error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request data");

        ApiResponse<Void> response = new ApiResponse<>(
                false,
                message,
                null
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles invalid JSON request bodies and enum conversion errors.
     *
     * @param exception unreadable HTTP message exception
     * @return 400 response with error message
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRequestBody(
            HttpMessageNotReadableException exception) {

        ApiResponse<Void> response = new ApiResponse<>(
                false,
                "Invalid request body or unsupported field value",
                null
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles unexpected application exceptions.
     *
     * @param exception unexpected exception
     * @return 500 response with generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception exception) {

        ApiResponse<Void> response = new ApiResponse<>(
                false,
                "An unexpected internal server error occurred",
                null
        );

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}