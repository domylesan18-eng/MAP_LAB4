package org.example.exceptions;

/**
 * Unchecked exception for a missing, unreadable or invalid configuration
 * (config.properties).
 */
public class ConfigException extends RuntimeException {
    /**
     * @param message description of the error
     */
    public ConfigException(String message) {
        super(message);
    }

    /**
     * @param message description of the error
     * @param cause the original exception (exception chaining)
     */
    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
