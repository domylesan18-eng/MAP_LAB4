package org.example.repository.db;

import org.example.exceptions.RepositoryException;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Wraps a {@link SQLException} (checked) into a {@link RepositoryException} (unchecked)
 * with a message the user can understand. The kind of error is recognised by its
 * SQLState (standard codes, the same for every JDBC driver), so the UI can show a specific
 * message - database stopped, wrong password, violated constraint - without a stack trace.
 * The original SQLException is kept as the cause (exception chaining).
 */
final class SqlErrors {
    private SqlErrors() {
    }

    /**
     * Builds the exception that replaces a {@link SQLException}.
     *
     * @param operation what the repository was doing, e.g. "salvarea utilizatorului ana"
     * @param e the original exception
     * @return a {@link RepositoryException} with a specific message and {@code e} as cause
     */
    static RepositoryException wrap(String operation, SQLException e) {
        String state = Optional.ofNullable(e.getSQLState()).orElse("");
        return new RepositoryException(describe(state, e) + " (la " + operation + ")", e);
    }

    // mesajul specific pentru fiecare categorie de eroare (SQLState)
    private static String describe(String state, SQLException e) {
        if (state.startsWith("08"))
            return "Baza de date nu este disponibila: serverul PostgreSQL este oprit sau db.url e gresit. "
                    + "Porniti serverul si reincercati [" + firstLine(e) + "]";
        if (state.equals("28P01") || state.equals("28000"))
            return "Conectare refuzata la baza de date: verificati db.user si db.password din config.properties";
        if (state.equals("3D000"))
            return "Baza de date din db.url nu exista: creati-o (vezi sql/socialnetwork.sql)";
        if (state.equals("42P01"))
            return "Tabelele lipsesc din baza de date: rulati sql/socialnetwork.sql";
        if (state.equals("23505"))
            return "Constrangere incalcata: exista deja o inregistrare cu aceeasi valoare unica - " + firstLine(e);
        if (state.equals("23503"))
            return "Constrangere incalcata: utilizatorul referit nu exista (cheie straina) - " + firstLine(e);
        if (state.equals("23514"))
            return "Constrangere incalcata (CHECK) - " + firstLine(e);
        if (state.equals("23502"))
            return "Constrangere incalcata: lipseste o valoare obligatorie (NOT NULL) - " + firstLine(e);
        if (state.startsWith("23"))
            return "Constrangere incalcata - " + firstLine(e);
        return "Eroare la baza de date (SQLState " + state + "): " + firstLine(e);
    }

    // mesajul driverului poate avea mai multe randuri (Detail, Position); primul e suficient
    private static String firstLine(SQLException e) {
        String message = Optional.ofNullable(e.getMessage()).orElse("");
        int end = message.indexOf('\n');
        return (end < 0 ? message : message.substring(0, end)).trim();
    }
}
