package com.leave.manager.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Generic API response DTO for returning standardized responses
 * from Manager Service REST APIs.
 *
 * @param <T> type of response data
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * Indicates whether the API operation was successful.
     */
    private boolean success;

    /**
     * Descriptive response message.
     */
    private String message;

    /**
     * Response data returned by the API.
     */
    private T data;
}