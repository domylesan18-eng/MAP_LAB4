package org.example.repository;

import org.example.domain.Entity;
import org.example.exceptions.RepositoryException;
import org.example.validation.Validator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Repository that keeps the entities in memory, in a map id -&gt; entity.
 * LinkedHashMap keeps the insertion order (the lines in the files keep their order).
 *
 * @param <ID> the type of the entity id
 * @param <E> the type of the entities
 */
public class InMemoryRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {
    /** the entities, by id; subclasses use it to restore the state when persisting fails */
    protected final Map<ID, E> entities = new LinkedHashMap<>();
    private final Validator<E> validator;

    /**
     * @param validator used on every save and update
     */
    public InMemoryRepository(Validator<E> validator) {
        this.validator = validator;
    }

    @Override
    public Optional<E> findOne(ID id) {
        if (id == null) throw new IllegalArgumentException("Id-ul nu poate fi null!");
        return Optional.ofNullable(entities.get(id));
    }

    @Override
    public Iterable<E> findAll() {
        return entities.values();
    }

    @Override
    public void save(E entity) {
        if (entity == null) throw new IllegalArgumentException("Entitatea nu poate fi null!");
        validator.validate(entity);
        if (entities.containsKey(entity.getId()))
            throw new RepositoryException("Exista deja o entitate cu id-ul " + entity.getId());
        entities.put(entity.getId(), entity);
    }

    @Override
    public E delete(ID id) {
        if (id == null) throw new IllegalArgumentException("Id-ul nu poate fi null!");
        E removed = entities.remove(id);
        if (removed == null)
            throw new RepositoryException("Nu exista entitatea cu id-ul " + id);
        return removed;
    }

    @Override
    public E update(E entity) {
        if (entity == null) throw new IllegalArgumentException("Entitatea nu poate fi null!");
        validator.validate(entity);
        if (!entities.containsKey(entity.getId()))
            throw new RepositoryException("Nu exista entitatea cu id-ul " + entity.getId());
        return entities.put(entity.getId(), entity);
    }
}
