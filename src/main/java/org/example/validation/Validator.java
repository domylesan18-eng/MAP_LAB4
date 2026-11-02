package org.example.validation;

import org.example.exceptions.ValidationException;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Validation strategy (Strategy pattern). Every entity type has its own validator,
 * which is given to the repository through the constructor.
 * <p>
 * It is a functional interface, so a new validation rule can be written as a lambda
 * and composed with an existing validator using {@link #andThen(Validator)}, without
 * writing a new class.
 *
 * @param <T> the type of the validated objects
 */
@FunctionalInterface
public interface Validator<T> {
    /**
     * Checks that the given object is valid.
     *
     * @param entity the object to validate
     * @throws ValidationException if the object is not valid
     */
    void validate(T entity);

    /**
     * Composes this validator with another one: the result validates first with this
     * validator and, if the object passes, with {@code other}.
     *
     * @param other the validator applied after this one, must not be null
     * @return a validator that applies both validators, in order
     * @throws NullPointerException if other is null
     */
    default Validator<T> andThen(Validator<T> other) {
        Objects.requireNonNull(other, "other");
        return entity -> {
            validate(entity);
            other.validate(entity);
        };
    }

    /**
     * Builds a validator from a condition that a valid object must satisfy.
     *
     * @param rule the condition; returns true for a valid object, must not be null
     * @param message the error message used when the condition is not satisfied
     * @param <T> the type of the validated objects
     * @return a validator that throws {@link ValidationException} with {@code message}
     *         when {@code rule} returns false
     * @throws NullPointerException if rule is null
     */
    static <T> Validator<T> of(Predicate<T> rule, String message) {
        Objects.requireNonNull(rule, "rule");
        return entity -> {
            if (!rule.test(entity)) throw new ValidationException(message);
        };
    }
}
