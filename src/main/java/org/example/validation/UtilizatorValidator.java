package org.example.validation;

import org.example.domain.Utilizator;
import org.example.exceptions.ValidationException;

import java.time.LocalDateTime;

/**
 * Validator for users: id &gt; 0, username made only of letters, digits and _,
 * first and last name not blank and without ';' (the field separator in the file),
 * account creation date present and not in the future.
 */
public class UtilizatorValidator implements Validator<Utilizator> {
    /**
     * @param u the user to validate
     * @throws ValidationException with all the errors found, if the user is not valid
     */
    @Override
    public void validate(Utilizator u) {
        StringBuilder errors = new StringBuilder();
        if (u.getId() == null || u.getId() <= 0) errors.append("Id invalid! ");
        if (u.getUsername() == null || !u.getUsername().matches("[A-Za-z0-9_]+"))
            errors.append("Username invalid (doar litere, cifre si _)! ");
        if (!isValidName(u.getFirstName())) errors.append("Prenume invalid! ");
        if (!isValidName(u.getLastName())) errors.append("Nume invalid! ");
        if (u.getCreatedAt() == null) errors.append("Data crearii contului lipseste! ");
        else if (u.getCreatedAt().isAfter(LocalDateTime.now()))
            errors.append("Data crearii contului nu poate fi in viitor! ");
        if (!errors.isEmpty()) throw new ValidationException(errors.toString().trim());
    }

    private boolean isValidName(String name) {
        return name != null && !name.isBlank() && !name.contains(";");
    }
}
