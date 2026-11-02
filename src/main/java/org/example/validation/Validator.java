package org.example.validation;

/**
 * Validation strategy (Strategy pattern). Every entity type has its own validator,
 * which is given to the repository through the constructor.
 *
 * @param <T> the type of the validated objects
 */
public interface Validator<T> {
    /**
     * Checks that the given object is valid.
     *
     * @param entity the object to validate
     * @throws org.example.exceptions.ValidationException if the object is not valid
     */
    void validate(T entity);
}
