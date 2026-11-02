package org.example.domain;

import java.util.Objects;

/**
 * Base class for all entities; every entity is identified by an id.
 *
 * @param <ID> the type of the id
 */
public abstract class Entity<ID> {
    private ID id;

    /**
     * @return the id of the entity
     */
    public ID getId() {
        return id;
    }

    /**
     * @param id the new id of the entity
     */
    public void setId(ID id) {
        this.id = id;
    }

    /**
     * Two entities are equal if they have the same concrete class and equal ids.
     *
     * @param o the object to compare with (may be null or of another type)
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> other = (Entity<?>) o;
        return Objects.equals(id, other.id);
    }

    /**
     * @return hash code consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
