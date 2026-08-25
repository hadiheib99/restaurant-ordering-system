package com.restaurant.ordering.exception;

/**
 * Domain exception thrown when a requested restaurant resource cannot be found.
 *
 * <p>{@link GlobalExceptionHandler} converts this exception to HTTP 404 for REST
 * clients.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Creates a not-found exception with a human-readable explanation.
     *
     * @param message human-readable description of the missing resource
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Creates a not-found exception that preserves an underlying cause.
     *
     * @param message human-readable description
     * @param cause original exception that caused the failure
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
