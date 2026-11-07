package com.leave.manager.exception;

/**
 * Custom exception thrown when communication with another
 * microservice fails.
 */
public class ServiceCommunicationException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructs an exception with the specified error message.
     *
     * @param message description of the communication failure
     */
    public ServiceCommunicationException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified message and cause.
     *
     * @param message description of the communication failure
     * @param cause original exception that caused the failure
     */
    public ServiceCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}