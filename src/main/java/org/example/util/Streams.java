package org.example.util;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Helper for streams. {@link Iterable} (returned by Repository.findAll()) has no
 * stream() method, so the conversion is written only once, here.
 */
public final class Streams {
    private Streams() {
    }

    /**
     * Creates a sequential stream over the elements of an iterable.
     *
     * @param iterable the source of the elements, must not be null
     * @param <T> the type of the elements
     * @return a new sequential stream over the elements; like any stream, it can be used only once
     */
    public static <T> Stream<T> of(Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
