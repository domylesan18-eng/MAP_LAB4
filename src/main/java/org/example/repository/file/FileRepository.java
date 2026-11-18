package org.example.repository.file;

import org.example.domain.Entity;
import org.example.exceptions.RepositoryException;
import org.example.exceptions.ValidationException;
import org.example.repository.InMemoryRepository;
import org.example.util.Streams;
import org.example.validation.Validator;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Repository that keeps the entities in memory and in a text file, one entity per line,
 * with the fields separated by ';'. The file is read once, when the repository is
 * created, and rewritten after every successful save, delete or update.
 * <p>
 * Composition instead of inheritance: the conversion between a line and an entity is done
 * by two functions received through the constructor (see {@link EntityConverters}).
 * <p>
 * A missing file means an empty repository; the file (and its folder) is created at the
 * first save. A corrupt or invalid line is reported, with its number and the reason,
 * and skipped.
 *
 * @param <ID> the type of the entity id
 * @param <E> the type of the entities
 */
public class FileRepository<ID, E extends Entity<ID>> extends InMemoryRepository<ID, E> {
    private final Path file;
    private final Function<String, E> lineToEntity;
    private final Function<E, String> entityToLine;

    /**
     * Creates a repository whose entities always come with their id and loads the file.
     *
     * @param validator used on every save and update, also for the loaded entities
     * @param fileName path of the file, relative to the working directory (e.g. data/prietenii.txt)
     * @param lineToEntity converts a line of the file into an entity
     * @param entityToLine converts an entity into a line of the file
     * @throws IllegalArgumentException if fileName is null or blank
     * @throws NullPointerException if one of the functions is null
     * @throws RepositoryException if the file exists but cannot be read
     */
    public FileRepository(Validator<E> validator, String fileName,
                          Function<String, E> lineToEntity, Function<E, String> entityToLine) {
        this(validator, null, fileName, lineToEntity, entityToLine);
    }

    /**
     * Creates the repository and loads the entities from the file.
     *
     * @param validator used on every save and update, also for the loaded entities
     * @param idGenerator generates the id of the entities saved without one (receives the
     *        ids already used); null if the entities always come with their id
     * @param fileName path of the file, relative to the working directory (e.g. data/utilizatori.txt)
     * @param lineToEntity converts a line of the file (without the line separator) into an entity;
     *        it may throw {@link RepositoryException}, {@link NumberFormatException} or
     *        {@link DateTimeParseException} for a corrupt line
     * @param entityToLine converts an entity into a line of the file (without the line separator)
     * @throws IllegalArgumentException if fileName is null or blank
     * @throws NullPointerException if one of the conversion functions is null
     * @throws RepositoryException if the file exists but cannot be read
     */
    public FileRepository(Validator<E> validator, Function<Collection<ID>, ID> idGenerator, String fileName,
                          Function<String, E> lineToEntity, Function<E, String> entityToLine) {
        super(validator, idGenerator);
        if (fileName == null || fileName.isBlank())
            throw new IllegalArgumentException("Numele fisierului nu poate fi gol!");
        this.file = Path.of(fileName);
        this.lineToEntity = Objects.requireNonNull(lineToEntity, "lineToEntity");
        this.entityToLine = Objects.requireNonNull(entityToLine, "entityToLine");
        loadData();
    }

    private void loadData() {
        if (!Files.exists(file)) return; // fisier lipsa: repo gol, se creeaza la primul save
        List<String> lines = readLines();
        // parcurg indicii ca sa stiu numarul liniei (i + 1)
        IntStream.range(0, lines.size())
                .filter(i -> !lines.get(i).isBlank())
                .forEach(i -> loadLine(lines.get(i), i + 1));
    }

    // Files.lines tine fisierul deschis, deci try-with-resources
    private List<String> readLines() {
        try (Stream<String> stream = Files.lines(file, StandardCharsets.UTF_8)) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            throw new RepositoryException("Nu pot citi fisierul " + file + ": " + e.getMessage(), e);
        } catch (UncheckedIOException e) { // eroare aparuta in timpul parcurgerii stream-ului
            throw new RepositoryException("Nu pot citi fisierul " + file + ": " + e.getCause().getMessage(), e);
        }
    }

    // o linie corupta nu opreste incarcarea: se raporteaza si se sare peste ea
    private void loadLine(String line, int lineNumber) {
        try {
            // super.save, nu save: altfel s-ar rescrie fisierul in timp ce il citim
            E entity = lineToEntity.apply(line);
            super.save(entity).ifPresent(duplicate ->
                    reportSkippedLine(lineNumber, line, "Exista deja o entitate cu id-ul " + duplicate.getId()));
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

    // rescrie tot fisierul din memorie: liniile se obtin printr-un stream peste entitati
    private void writeToFile() {
        List<String> lines = Streams.of(findAll())
                .map(entityToLine)
                .collect(Collectors.toList());
        try {
            Path folder = file.getParent();
            if (folder != null) Files.createDirectories(folder);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RepositoryException("Nu pot scrie fisierul " + file + ": " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     * If the file cannot be written, the entity is removed from memory as well.
     */
    @Override
    public Optional<E> save(E entity) {
        Optional<E> result = super.save(entity);
        if (result.isPresent()) return result; // id deja folosit: nimic de scris
        try {
            writeToFile();
        } catch (RepositoryException e) {
            entities.remove(entity.getId()); // memoria ramane la fel ca fisierul
            throw e;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * If the file cannot be written, the entity is put back in memory.
     */
    @Override
    public Optional<E> delete(ID id) {
        Optional<E> removed = super.delete(id);
        removed.ifPresent(entity -> {
            try {
                writeToFile();
            } catch (RepositoryException e) {
                entities.put(id, entity);
                throw e;
            }
        });
        return removed;
    }

    /**
     * {@inheritDoc}
     * If the file cannot be written, the old version is put back in memory.
     */
    @Override
    public Optional<E> update(E entity) {
        E old = entity == null ? null : entities.get(entity.getId());
        Optional<E> result = super.update(entity);
        if (result.isPresent()) return result; // id inexistent: nimic de scris
        try {
            writeToFile();
        } catch (RepositoryException e) {
            entities.put(old.getId(), old);
            throw e;
        }
        return result;
    }
}
