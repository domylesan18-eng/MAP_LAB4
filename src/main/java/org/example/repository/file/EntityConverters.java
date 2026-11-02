package org.example.repository.file;

import org.example.domain.Prietenie;
import org.example.domain.Utilizator;
import org.example.exceptions.RepositoryException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Conversions between a line of a data file and an entity. They are passed to
 * {@link FileRepository} as method references, e.g.
 * {@code EntityConverters::utilizatorFromLine}. The file formats are the ones from lab 3:
 * <pre>
 *   utilizatori.txt   id;prenume;nume;username;dataCrearii   ex. 1;Ana;Pop;ana;2026-09-01T10:00
 *   prietenii.txt     id1;id2;data                           ex. 1;2;2026-10-19T10:30
 * </pre>
 */
public final class EntityConverters {
    /** separator of the fields on a line */
    public static final String SEPARATOR = ";";

    private EntityConverters() {
    }

    /**
     * Converts a line of utilizatori.txt into a user.
     *
     * @param line the line, without the line separator
     * @return the user
     * @throws RepositoryException if the line does not have 5 fields
     * @throws NumberFormatException if the id is not a number
     * @throws DateTimeParseException if the date is not an ISO date
     */
    public static Utilizator utilizatorFromLine(String line) {
        String[] fields = fields(line, 5);
        return new Utilizator(Long.parseLong(fields[0].trim()), fields[3].trim(), fields[1].trim(), fields[2].trim(),
                LocalDateTime.parse(fields[4].trim()));
    }

    /**
     * Converts a user into a line of utilizatori.txt.
     *
     * @param u the user
     * @return the line id;prenume;nume;username;dataCrearii
     */
    public static String utilizatorToLine(Utilizator u) {
        return String.join(SEPARATOR, String.valueOf(u.getId()), u.getFirstName(), u.getLastName(),
                u.getUsername(), String.valueOf(u.getCreatedAt()));
    }

    /**
     * Converts a line of prietenii.txt into a friendship.
     *
     * @param line the line, without the line separator
     * @return the friendship
     * @throws RepositoryException if the line does not have 3 fields
     * @throws NumberFormatException if an id is not a number
     * @throws DateTimeParseException if the date is not an ISO date
     */
    public static Prietenie prietenieFromLine(String line) {
        String[] fields = fields(line, 3);
        return new Prietenie(Long.parseLong(fields[0].trim()), Long.parseLong(fields[1].trim()),
                LocalDateTime.parse(fields[2].trim()));
    }

    /**
     * Converts a friendship into a line of prietenii.txt.
     *
     * @param p the friendship
     * @return the line id1;id2;data
     */
    public static String prietenieToLine(Prietenie p) {
        return String.join(SEPARATOR, String.valueOf(p.getIdUser1()), String.valueOf(p.getIdUser2()),
                String.valueOf(p.getFriendsFrom()));
    }

    // imparte linia dupa ';' (pastrand campurile goale) si verifica numarul de campuri
    private static String[] fields(String line, int expected) {
        String[] fields = line.split(SEPARATOR, -1);
        if (fields.length != expected)
            throw new RepositoryException("asteptam " + expected + " campuri, am gasit " + fields.length);
        return fields;
    }
}
