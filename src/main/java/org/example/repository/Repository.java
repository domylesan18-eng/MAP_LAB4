package org.example.repository;

import org.example.domain.Entity;

import java.util.Optional;

/**
 * CRUD operations on a repository of entities.
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
     */
    Optional<E> findOne(ID id);

    /**
     * @return all the entities
     */
    Iterable<E> findAll();

    /**
     * Adds a new entity.
     *
     * @param entity the entity to add, must not be null
     * @throws IllegalArgumentException if entity is null
     * @throws org.example.exceptions.ValidationException if the entity is not valid
     * @throws org.example.exceptions.RepositoryException if the id is already used
     *         or the entity could not be persisted
     */
    void save(E entity);

    /**
     * Removes the entity with the given id.
     *
     * @param id the id of the entity, must not be null
     * @return the removed entity
     * @throws IllegalArgumentException if id is null
     * @throws org.example.exceptions.RepositoryException if there is no entity with this id
     *         or the change could not be persisted
     */
    E delete(ID id);

    /**
     * Replaces the entity that has the same id as the given one.
     *
     * @param entity the new version of the entity, must not be null
     * @return the old version of the entity
     * @throws IllegalArgumentException if entity is null
     * @throws org.example.exceptions.ValidationException if the entity is not valid
     * @throws org.example.exceptions.RepositoryException if there is no entity with this id
     *         or the change could not be persisted
     */
    E update(E entity);
}
