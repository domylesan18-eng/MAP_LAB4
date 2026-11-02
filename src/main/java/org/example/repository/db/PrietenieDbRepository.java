package org.example.repository.db;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.repository.Repository;
import org.example.validation.Validator;

import java.util.Optional;

/**
 * Friendships in a relational database. Skeleton only: the JDBC code is written in lab 5,
 * until then every operation throws {@link UnsupportedOperationException}.
 */
public class PrietenieDbRepository implements Repository<Tuple<Long, Long>, Prietenie> {
    private static final String NOT_IMPLEMENTED =
            "Persistenta in baza de date nu este implementata inca (laboratorul 5).";

    private final Validator<Prietenie> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * @param validator the validator for the entities
     * @param url the JDBC url of the database (db.url)
     * @param user the database user (db.user)
     * @param password the database password (db.password)
     */
    public PrietenieDbRepository(Validator<Prietenie> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * @throws UnsupportedOperationException always, until lab 5
     */
    @Override
    public Optional<Prietenie> findOne(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 5
     */
    @Override
    public Iterable<Prietenie> findAll() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 5
     */
    @Override
    public void save(Prietenie entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 5
     */
    @Override
    public Prietenie delete(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * @throws UnsupportedOperationException always, until lab 5
     */
    @Override
    public Prietenie update(Prietenie entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
