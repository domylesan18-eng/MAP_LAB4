package org.example.exceptions;

/**
 * Unchecked exception for persistence errors: duplicate or missing id, unreadable or
 * unwritable file, corrupt line. The original exception (e.g. IOException) is kept as the cause.
 */
public class RepositoryException extends RuntimeException {
    /**
     * @param message description of the error
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * @param message description of the error
     * @param cause the original exception (exception chaining)
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
