package org.example.repository.db;

import org.example.domain.Utilizator;
import org.example.repository.Repository;
import org.example.validation.Validator;

import java.util.Optional;

/**
 * Users in a relational database. Skeleton only: the JDBC code is written in lab 4,
 * until then every operation throws {@link UnsupportedOperationException}.
 */
public class UtilizatorDbRepository implements Repository<Long, Utilizator> {
    private static final String NOT_IMPLEMENTED =
            "Persistenta in baza de date nu este implementata inca (laboratorul 4).";

    private final Validator<Utilizator> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * @param validator the validator for the entities
     * @param url the JDBC url of the database (db.url)
     * @param user the database user (db.user)
     * @param password the database password (db.password)
     */
    public UtilizatorDbRepository(Validator<Utilizator> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * @throws UnsupportedOperationException always, until lab 4
     */
    @Override
    public Optional<Utilizator> findOne(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 4
     */
    @Override
    public Iterable<Utilizator> findAll() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 4
     */
    @Override
    public void save(Utilizator entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 4
     */
    @Override
    public Utilizator delete(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 4
     */
    @Override
    public Utilizator update(Utilizator entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
