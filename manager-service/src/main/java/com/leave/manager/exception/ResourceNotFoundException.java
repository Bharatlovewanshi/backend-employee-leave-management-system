package com.leave.manager.exception;

/**
 * Custom exception thrown when a requested resource
 * cannot be found in the Manager Service.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructs an exception with the specified error message.
     *
     * @param message description of the resource not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}