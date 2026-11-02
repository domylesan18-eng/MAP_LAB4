package org.example.repository.file;

import org.example.domain.Utilizator;
import org.example.validation.Validator;

import java.time.LocalDateTime;

/**
 * Users in a text file, one per line: id;prenume;nume;username;dataCrearii
 * (the date in ISO format, e.g. 2026-10-01T09:00).
 */
public class UtilizatorFileRepository extends AbstractFileRepository<Long, Utilizator> {
    /**
     * @param validator the user validator
     * @param fileName path of the file, e.g. data/utilizatori.txt
     * @throws IllegalArgumentException if fileName is null or blank
     * @throws org.example.exceptions.RepositoryException if the file exists but cannot be read
     */
    public UtilizatorFileRepository(Validator<Utilizator> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Utilizator extractEntity(String[] fields) {
        checkFieldCount(fields, 5);
        return new Utilizator(Long.parseLong(fields[0].trim()), fields[3].trim(), fields[1].trim(), fields[2].trim(),
                LocalDateTime.parse(fields[4].trim()));
    }

    @Override
    protected String createEntityAsString(Utilizator u) {
        return u.getId() + SEPARATOR + u.getFirstName() + SEPARATOR + u.getLastName() + SEPARATOR + u.getUsername()
                + SEPARATOR + u.getCreatedAt();
    }
}
