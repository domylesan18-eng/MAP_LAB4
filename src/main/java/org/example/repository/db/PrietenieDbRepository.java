package org.example.repository.db;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.exceptions.RepositoryException;
import org.example.repository.Repository;
import org.example.validation.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Friendships stored in the PostgreSQL table {@code friendships}
 * (id1, id2, friends_from), primary key (id1, id2), CHECK (id1 &lt; id2), id1 and id2 foreign
 * keys to users(id) with ON DELETE CASCADE.
 * <p>
 * The id of a friendship is the pair of user ids, already normalized by
 * {@link Prietenie#idOf(Long, Long)} (smaller id first), so it matches CHECK (id1 &lt; id2).
 * Every method opens its own Connection, PreparedStatement and ResultSet in
 * try-with-resources; the values are always parameters (?) of a PreparedStatement;
 * {@link SQLException} is wrapped into {@link RepositoryException}.
 */
public class PrietenieDbRepository implements Repository<Tuple<Long, Long>, Prietenie> {
    private static final String SELECT_ONE =
            "SELECT id1, id2, friends_from FROM friendships WHERE id1 = ? AND id2 = ?";
    private static final String SELECT_ALL =
            "SELECT id1, id2, friends_from FROM friendships ORDER BY id1, id2";
    // ON CONFLICT DO NOTHING: o prietenie existenta nu e o eroare, save intoarce entitatea
    private static final String INSERT =
            "INSERT INTO friendships (id1, id2, friends_from) VALUES (?, ?, ?) ON CONFLICT (id1, id2) DO NOTHING";
    private static final String DELETE =
            "DELETE FROM friendships WHERE id1 = ? AND id2 = ? RETURNING id1, id2, friends_from";
    private static final String UPDATE =
            "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?";

    private final Validator<Prietenie> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * Creates the repository. No connection is opened here: every operation opens its
     * own connection.
     *
     * @param validator the validator for the friendships, used before save and update
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
     * @return a new connection to the database; the caller closes it
     * @throws SQLException if the connection cannot be opened (e.g. server stopped)
     */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Builds a friendship from the current row of the result set.
     *
     * @param rs a result set positioned on a row of friendships
     * @return the friendship from the row
     * @throws SQLException if a column cannot be read
     */
    private static Prietenie extract(ResultSet rs) throws SQLException {
        return new Prietenie(rs.getLong("id1"), rs.getLong("id2"),
                rs.getTimestamp("friends_from").toLocalDateTime());
    }

    /**
     * Sets the two ids of a friendship on consecutive parameters.
     *
     * @param ps the statement
     * @param first the position of the parameter for id1 (id2 is on the next one)
     * @param id the id of the friendship (already normalized: smaller id first)
     * @throws SQLException if a parameter cannot be set
     */
    private static void setIds(PreparedStatement ps, int first, Tuple<Long, Long> id) throws SQLException {
        ps.setLong(first, id.getLeft());
        ps.setLong(first + 1, id.getRight());
    }

    /**
     * Checks that the id of a friendship and both its components are not null.
     *
     * @param id the id to check
     * @throws IllegalArgumentException if id or one of its components is null
     */
    private static void requireId(Tuple<Long, Long> id) {
        if (id == null || id.getLeft() == null || id.getRight() == null)
            throw new IllegalArgumentException("Id-ul prieteniei nu poate fi null!");
    }

    /**
     * {@inheritDoc}
     *
     * @param id the pair of user ids (smaller first), must not be null
     * @return the friendship, or an empty Optional if there is no such row
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if the database cannot be queried
     */
    @Override
    public Optional<Prietenie> findOne(Tuple<Long, Long> id) {
        requireId(id);
        Tuple<Long, Long> normalized = Prietenie.idOf(id.getLeft(), id.getRight());
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ONE)) {
            setIds(ps, 1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(extract(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw SqlErrors.wrap("cautarea prieteniei " + normalized, e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return all the friendships, ordered by id1, id2
     * @throws RepositoryException if the database cannot be queried
     */
    @Override
    public Iterable<Prietenie> findAll() {
        List<Prietenie> friendships = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) friendships.add(extract(rs));
            return friendships;
        } catch (SQLException e) {
            throw SqlErrors.wrap("citirea prieteniilor", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param entity the friendship, must not be null
     * @return an empty Optional if it was saved, or an Optional with {@code entity} if the
     *         two users are already friends (executeUpdate() returns 0)
     * @throws IllegalArgumentException if entity is null
     * @throws org.example.exceptions.ValidationException if the friendship is not valid
     * @throws RepositoryException if the insert fails (e.g. database stopped, a user does
     *         not exist - foreign key constraint)
     */
    @Override
    public Optional<Prietenie> save(Prietenie entity) {
        if (entity == null) throw new IllegalArgumentException("Entitatea nu poate fi null!");
        validator.validate(entity);
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT)) {
            setIds(ps, 1, entity.getId());
            ps.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            int rows = ps.executeUpdate();
            return rows == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw SqlErrors.wrap("salvarea prieteniei " + entity.getId(), e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param id the pair of user ids, must not be null
     * @return the deleted friendship, or an empty Optional if there is no such row
     * @throws IllegalArgumentException if id is null
     * @throws RepositoryException if the delete fails
     */
    @Override
    public Optional<Prietenie> delete(Tuple<Long, Long> id) {
        requireId(id);
        Tuple<Long, Long> normalized = Prietenie.idOf(id.getLeft(), id.getRight());
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE)) {
            setIds(ps, 1, normalized);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(extract(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw SqlErrors.wrap("stergerea prieteniei " + normalized, e);
        }
    }

    /**
     * Changes the date of a friendship.
     *
     * @param entity the new version of the friendship, must not be null
     * @return an empty Optional if the row was updated, or an Optional with {@code entity}
     *         if there is no such friendship (executeUpdate() returns 0)
     * @throws IllegalArgumentException if entity is null
     * @throws org.example.exceptions.ValidationException if the friendship is not valid
     * @throws RepositoryException if the update fails
     */
    @Override
    public Optional<Prietenie> update(Prietenie entity) {
        if (entity == null) throw new IllegalArgumentException("Entitatea nu poate fi null!");
        requireId(entity.getId());
        validator.validate(entity);
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setTimestamp(1, Timestamp.valueOf(entity.getFriendsFrom()));
            setIds(ps, 2, entity.getId());
            int rows = ps.executeUpdate();
            return rows == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw SqlErrors.wrap("modificarea prieteniei " + entity.getId(), e);
        }
    }
}
