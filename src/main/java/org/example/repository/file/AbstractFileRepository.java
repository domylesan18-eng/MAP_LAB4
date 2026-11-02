package org.example.repository.file;

import org.example.domain.Entity;
import org.example.exceptions.RepositoryException;
import org.example.exceptions.ValidationException;
import org.example.repository.InMemoryRepository;
import org.example.validation.Validator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;

/**
 * Repository that keeps the entities in memory and in a text file, one entity per line,
 * with the fields separated by ';'. The file is read once, when the repository is
 * created, and rewritten after every successful save, delete or update.
 * <p>
 * Template Method: the algorithms for reading and writing the file are here; the
 * subclasses only convert between a line and an entity ({@link #extractEntity} and
 * {@link #createEntityAsString}).
 * <p>
 * A missing file means an empty repository; the file (and its folder) is created at the
 * first save. A corrupt or invalid line is reported, with its number and the reason,
 * and skipped.
 * <p>
 * Note: the constructor calls {@link #extractEntity}, so the subclasses must not use
 * their own fields in it (they are not initialized yet, Effective Java, Item 19).
 *
 * @param <ID> the type of the entity id
 * @param <E> the type of the entities
 */
public abstract class AbstractFileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    /** separator of the fields on a line */
    protected static final String SEPARATOR = ";";

    private final Path file;

    /**
     * Creates the repository and loads the entities from the file.
     *
     * @param validator used on every save and update, also for the loaded entities
     * @param fileName path of the file, relative to the working directory (e.g. data/utilizatori.txt)
     * @throws IllegalArgumentException if fileName is null or blank
     * @throws RepositoryException if the file exists but cannot be read
     */
    protected AbstractFileRepository(Validator<E> validator, String fileName) {
        super(validator);
        if (fileName == null || fileName.isBlank())
            throw new IllegalArgumentException("Numele fisierului nu poate fi gol!");
        this.file = Path.of(fileName);
        loadData();
    }

    /**
     * Converts the fields of a line into an entity.
     *
     * @param fields the fields of the line (the line split by ';', empty fields included)
     * @return the entity
     * @throws RepositoryException if the number of fields is wrong
     * @throws NumberFormatException if a numeric field is not a number
     * @throws DateTimeParseException if a date field is not an ISO date
     */
    protected abstract E extractEntity(String[] fields);

    /**
     * Converts an entity into a line of the file (without the line separator).
     *
     * @param entity the entity
     * @return the line, with the fields separated by ';'
     */
    protected abstract String createEntityAsString(E entity);

    /**
     * Checks the number of fields of a line, for use in {@link #extractEntity}.
     *
     * @param fields the fields of the line
     * @param expected the expected number of fields
     * @throws RepositoryException if the number of fields is different
     */
    protected static void checkFieldCount(String[] fields, int expected) {
        if (fields.length != expected)
            throw new RepositoryException("asteptam " + expected + " campuri, am gasit " + fields.length);
    }

    private void loadData() {
        if (!Files.exists(file)) return; // fisier lipsa: repo gol, se creeaza la primul save
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) continue;
                loadLine(line, lineNumber);
            }
        } catch (IOException e) {
            throw new RepositoryException("Nu pot citi fisierul " + file + ": " + e.getMessage(), e);
        }
    }

    // o linie corupta nu opreste incarcarea: se raporteaza si se sare peste ea
    private void loadLine(String line, int lineNumber) {
        try {
            // super.save, nu save: altfel s-ar rescrie fisierul in timp ce il citim
            super.save(extractEntity(line.split(SEPARATOR, -1)));
        } catch (NumberFormatException e) {
            reportSkippedLine(lineNumber, line, "numar invalid (" + e.getMessage() + ")");
        } catch (DateTimeParseException e) {
            reportSkippedLine(lineNumber, line, "data invalida, se asteapta formatul ISO, ex. 2026-10-19T10:30");
        } catch (ValidationException | RepositoryException e) {
            reportSkippedLine(lineNumber, line, e.getMessage());
        }
    }

    private void reportSkippedLine(int lineNumber, String line, String reason) {
        // pe System.out, ca mesajele sa apara in ordine, inainte de meniu
        System.out.println("Atentie: " + file + ", linia " + lineNumber + " ignorata: " + reason + " -> \"" + line + "\"");
    }

    // rescrie tot fisierul din memorie
    private void writeToFile() {
        try {
            Path folder = file.getParent();
            if (folder != null) Files.createDirectories(folder);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                for (E entity : findAll()) {
                    writer.write(createEntityAsString(entity));
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new RepositoryException("Nu pot scrie fisierul " + file + ": " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * If the file cannot be written, the entity is removed from memory as well.
     */
    @Override
    public void save(E entity) {
        super.save(entity);
        try {
            writeToFile();
        } catch (RepositoryException e) {
            entities.remove(entity.getId()); // memoria ramane la fel ca fisierul
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     * If the file cannot be written, the entity is put back in memory.
     */
    @Override
    public E delete(ID id) {
        E removed = super.delete(id);
        try {
            writeToFile();
        } catch (RepositoryException e) {
            entities.put(id, removed);
            throw e;
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     * If the file cannot be written, the old version is put back in memory.
     */
    @Override
    public E update(E entity) {
        E old = super.update(entity);
        try {
            writeToFile();
        } catch (RepositoryException e) {
            entities.put(old.getId(), old);
            throw e;
        }
        return old;
    }
}
