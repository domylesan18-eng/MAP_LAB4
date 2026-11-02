package org.example.factory;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.domain.Utilizator;
import org.example.exceptions.ConfigException;
import org.example.repository.InMemoryRepository;
import org.example.repository.Repository;
import org.example.repository.db.PrietenieDbRepository;
import org.example.repository.db.UtilizatorDbRepository;
import org.example.repository.file.PrietenieFileRepository;
import org.example.repository.file.UtilizatorFileRepository;
import org.example.validation.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;

/**
 * Simple Factory (and Singleton) for the repositories. It is the only class of the
 * application that knows the concrete repository classes; the rest of the application
 * only sees {@link Repository}.
 * <p>
 * The persistence type and its parameters are read from config.properties
 * (src/main/resources): repository.type, file.utilizatori, file.prietenii, db.url,
 * db.user, db.password. Both repositories are created for the same persistence type.
 */
public final class RepositoryFactory {
    private static final String CONFIG_FILE = "config.properties";
    private static RepositoryFactory instance;

    private final Properties config;
    private final RepositoryType type;

    private RepositoryFactory() {
        config = loadConfig();
        type = readType();
    }

    /**
     * @return the single instance of the factory, created (and configured) at the first call
     * @throws ConfigException if config.properties is missing, unreadable, or
     *         repository.type is missing or unknown
     */
    public static synchronized RepositoryFactory getInstance() {
        if (instance == null) instance = new RepositoryFactory();
        return instance;
    }

    /**
     * @return the persistence type read from the configuration
     */
    public RepositoryType getType() {
        return type;
    }

    /**
     * Creates the user repository for the configured persistence type.
     *
     * @param validator the user validator
     * @return the user repository
     * @throws ConfigException if a parameter needed by the persistence type is missing
     * @throws org.example.exceptions.RepositoryException if the data file cannot be read
     */
    public Repository<Long, Utilizator> createUtilizatorRepository(Validator<Utilizator> validator) {
        switch (type) {
            case MEMORY:
                return new InMemoryRepository<>(validator);
            case FILE:
                return new UtilizatorFileRepository(validator, require("file.utilizatori"));
            case DATABASE:
                return new UtilizatorDbRepository(validator, require("db.url"), require("db.user"),
                        requirePresent("db.password"));
            default:
                throw new ConfigException("Tip de persistenta netratat: " + type);
        }
    }

    /**
     * Creates the friendship repository for the configured persistence type.
     *
     * @param validator the friendship validator
     * @return the friendship repository
     * @throws ConfigException if a parameter needed by the persistence type is missing
     * @throws org.example.exceptions.RepositoryException if the data file cannot be read
     */
    public Repository<Tuple<Long, Long>, Prietenie> createPrietenieRepository(Validator<Prietenie> validator) {
        switch (type) {
            case MEMORY:
                return new InMemoryRepository<>(validator);
            case FILE:
                return new PrietenieFileRepository(validator, require("file.prietenii"));
            case DATABASE:
                return new PrietenieDbRepository(validator, require("db.url"), require("db.user"),
                        requirePresent("db.password"));
            default:
                throw new ConfigException("Tip de persistenta netratat: " + type);
        }
    }

    // citeste config.properties din resources
    private static Properties loadConfig() {
        InputStream input = RepositoryFactory.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (input == null)
            throw new ConfigException("Nu am gasit fisierul de configurare " + CONFIG_FILE
                    + " (trebuie sa fie in src/main/resources).");
        Properties properties = new Properties();
        try (Reader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            properties.load(reader);
        } catch (IOException e) {
            throw new ConfigException("Nu pot citi " + CONFIG_FILE + ": " + e.getMessage(), e);
        }
        return properties;
    }

    // transforma repository.type in enum
    private RepositoryType readType() {
        String value = require("repository.type");
        try {
            return RepositoryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigException("Valoare necunoscuta pentru repository.type in " + CONFIG_FILE + ": '"
                    + value + "'. Valori permise: " + Arrays.toString(RepositoryType.values()), e);
        }
    }

    // valoare obligatorie si nevida
    private String require(String key) {
        String value = config.getProperty(key);
        if (value == null || value.isBlank())
            throw new ConfigException("Lipseste proprietatea '" + key + "' (sau e goala) in " + CONFIG_FILE + ".");
        return value.trim();
    }

    // valoare obligatorie, dar poate fi goala (ex. parola goala)
    private String requirePresent(String key) {
        String value = config.getProperty(key);
        if (value == null)
            throw new ConfigException("Lipseste proprietatea '" + key + "' in " + CONFIG_FILE + ".");
        return value;
    }
}
