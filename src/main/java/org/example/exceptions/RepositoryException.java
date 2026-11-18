package org.example.exceptions;

/**
 * Unchecked exception for persistence errors: unreadable or unwritable file, corrupt line,
 * database stopped, violated constraint. The original checked exception (IOException,
 * SQLException) is kept as the cause, so the Repository interface does not depend on the
 * kind of storage.
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
