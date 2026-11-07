package com.leave.admin.exception;

public class AdminOperationException extends RuntimeException {

    public AdminOperationException(String message) {
        super(message);
    }

    public AdminOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}