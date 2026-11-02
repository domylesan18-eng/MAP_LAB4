package org.example.domain;

import java.util.Objects;

/**
 * An immutable pair of two values. Used as the id of a friendship.
 *
 * @param <E1> type of the left value
 * @param <E2> type of the right value
 */
public class Tuple<E1, E2> {
    private final E1 left;
    private final E2 right;

    /**
     * @param left the left value
     * @param right the right value
     */
    public Tuple(E1 left, E2 right) {
        this.left = left;
        this.right = right;
    }

    /**
     * @return the left value
     */
    public E1 getLeft() {
        return left;
    }

    /**
     * @return the right value
     */
    public E2 getRight() {
        return right;
    }

    /**
     * Two tuples are equal if both components are equal.
     *
     * @param o the object to compare with (may be null or of another type)
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tuple<?, ?> other = (Tuple<?, ?>) o;
        return Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

    /**
     * @return hash code consistent with {@link #equals(Object)}
     */
    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public String toString() {
        return "(" + left + ", " + right + ")";
    }
}
