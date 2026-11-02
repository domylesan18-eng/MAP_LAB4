package org.example.exceptions;

/**
 * Unchecked exception thrown by validators when an entity is not valid.
 */
public class ValidationException extends RuntimeException {
    /**
     * @param message description of what is invalid
     */
    public ValidationException(String message) {
        super(message);
    }
}
