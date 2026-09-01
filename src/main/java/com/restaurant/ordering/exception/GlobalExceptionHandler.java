package com.restaurant.ordering.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts backend exceptions into consistent JSON error responses for REST clients.
 *
 * <p>The handler maps authentication, not-found, validation, authorization,
 * integrity and generic errors to appropriate HTTP status codes while keeping
 * the response structure consistent for the Angular frontend.</p>
 *
 * @author Abdulhadi Heib
 * @version 1.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles invalid login credentials reported by Spring Security.
     *
     * @param ex authentication failure
     * @return HTTP 401 response with a client-safe credential message
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return error(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    /**
     * Handles requests for domain resources that do not exist.
     *
     * @param ex missing-resource exception
     * @return HTTP 404 response containing the standardized error body
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * Handles invalid arguments and rejected business input.
     *
     * @param ex invalid business or input argument
     * @return HTTP 400 response containing the standardized error body
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /**
     * Handles authorization failures raised by the security or service layer.
     *
     * @param ex authorization failure
     * @return HTTP 403 response containing the standardized error body
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    /**
     * Converts bean-validation failures to a concise field-level message.
     *
     * @param ex Spring MVC validation exception
     * @return HTTP 400 response containing the first field-validation message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Validation failed");
        return error(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Handles database integrity conflicts such as invalid deletes or duplicate data.
     *
     * @param ex database integrity conflict
     * @return HTTP 409 response containing a client-safe conflict message
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(DataIntegrityViolationException ex) {
        return error(HttpStatus.CONFLICT, "The operation conflicts with existing restaurant data");
    }

    /**
     * Handles a write that targets data changed by another request.
     *
     * @param ex stale write failure
     * @return HTTP 409 response asking the client to retry with fresh data
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<Map<String, Object>> handleConcurrentUpdate(ObjectOptimisticLockingFailureException ex) {
        return error(HttpStatus.CONFLICT, "The data changed while this request was being processed; refresh and try again");
    }

    /**
     * Handles otherwise unhandled server exceptions without exposing internals.
     *
     * @param ex unhandled server exception
     * @return HTTP 500 response containing a generic client-safe message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected server error occurred");
    }

    /**
     * Builds the common timestamp/status/error/message response body.
     *
     * @param status HTTP status returned to the client
     * @param message human-readable error message
     * @return response entity containing the standardized JSON error structure
     */
    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String message) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("message", message);
        return ResponseEntity.status(status).body(response);
    }
}
