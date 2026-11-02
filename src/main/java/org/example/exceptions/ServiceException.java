package org.example.exceptions;

/**
 * Unchecked exception for business rule errors, e.g. username already taken
 * or users that are already friends.
 */
public class ServiceException extends RuntimeException {
    /**
     * @param message description of the error
     */
    public ServiceException(String message) {
        super(message);
    }
}
