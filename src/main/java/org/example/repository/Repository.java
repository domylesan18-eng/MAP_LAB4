package org.example.repository;

import org.example.domain.Entity;
import org.example.util.Streams;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * CRUD operations on a repository of entities.
 * <p>
 * Lab 5: the contract follows the one from the course (UtilizatorDbRepository): the results
 * that may be missing are {@link Optional}. save, delete and update no longer throw an
 * exception when the id does not exist / already exists; they report it through the
 * returned Optional, which matches what the database tells us (the number of rows changed
 * by executeUpdate()). {@link org.example.exceptions.RepositoryException} is kept for real
 * persistence errors (database stopped, violated constraint, unwritable file).
 *
 * @param <ID> the type of the entity id
 * @param <E> the type of the entities
 */
public interface Repository<ID, E extends Entity<ID>> {
    /**
     * Finds the entity with the given id.
     *
     * @param id the id of the entity, must not be null
     * @return the entity, or an empty Optional if there is no entity with this id
     * @throws IllegalArgumentException if id is null
     * @throws org.example.exceptions.RepositoryException if the storage cannot be read
     */
    Optional<E> findOne(ID id);

    /**
     * @return all the entities
     * @throws org.example.exceptions.RepositoryException if the storage cannot be read
     */
    Iterable<E> findAll();

    /**
     * Finds all the entities that satisfy a condition. The search criteria are given
     * as predicates (see {@link org.example.service.CriteriiUtilizator}), so the
     * repository needs no findByXxx method for every field.
     *
     * @param filter the condition an entity must satisfy to be returned, must not be null
     * @return a new list with the matching entities, in the order of {@link #findAll()}
     *         (empty if there is none)
     * @throws NullPointerException if filter is null
     * @throws org.example.exceptions.RepositoryException if the storage cannot be read
     */
    default List<E> findAll(Predicate<E> filter) {
        return Streams.of(findAll())
                .filter(filter)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new entity. If the entity has no id (null) and the storage generates ids,
     * the generated id is set on the given entity.
     *
     * @param entity the entity to add, must not be null
     * @return an empty Optional if the entity was saved, or an Optional with the entity
     *         if an entity with the same id already exists (nothing is changed)
     * @throws IllegalArgumentException if entity is null, or its id is null and the
     *         storage does not generate ids
     * @throws org.example.exceptions.ValidationException if the entity is not valid
     * @throws org.example.exceptions.RepositoryException if the entity could not be persisted
     *         (e.g. database stopped, violated constraint, unwritable file)
     */
    Optional<E> save(E entity);

    /**
     * Removes the entity with the given id.
     *
     * @param id the id of the entity, must not be null
     * @return an Optional with the removed entity, or an empty Optional if there is
     *         no entity with this id
     * @throws IllegalArgumentException if id is null
     * @throws org.example.exceptions.RepositoryException if the change could not be persisted
     */
    Optional<E> delete(ID id);

    /**
     * Replaces the entity that has the same id as the given one.
     *
     * @param entity the new version of the entity, must not be null
     * @return an empty Optional if the entity was updated, or an Optional with the given
     *         entity if there is no entity with this id (nothing is changed)
     * @throws IllegalArgumentException if entity or its id is null
     * @throws org.example.exceptions.ValidationException if the entity is not valid
     * @throws org.example.exceptions.RepositoryException if the change could not be persisted
     */
    Optional<E> update(E entity);
}
