package com.leave.manager.exception;

/**
 * Custom exception thrown when a manager submits
 * an invalid leave approval or rejection decision.
 */
public class InvalidDecisionException extends RuntimeException {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Constructs an exception with the specified error message.
     *
     * @param message description of the invalid decision
     */
    public InvalidDecisionException(String message) {
        super(message);
    }
}