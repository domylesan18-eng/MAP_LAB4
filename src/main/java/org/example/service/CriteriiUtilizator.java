package org.example.service;

import org.example.domain.Utilizator;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Search criteria for users, as {@link Predicate}s. They are given to
 * {@link org.example.repository.Repository#findAll(Predicate)} and can be combined with
 * {@code and}, {@code or}, {@code negate} and {@link Predicate#not}, so no
 * findByLastName-like method is needed in the repository. A new criterion can also be
 * written directly as a lambda, without changing this class.
 */
public final class CriteriiUtilizator {
    private CriteriiUtilizator() {
    }

    /**
     * @param username the username to look for (case sensitive)
     * @return a predicate true for the user with exactly this username
     */
    public static Predicate<Utilizator> areUsername(String username) {
        return u -> u.getUsername().equals(username);
    }

    /**
     * @param text the text to look for (case insensitive)
     * @return a predicate true for the users whose username contains the text
     */
    public static Predicate<Utilizator> usernameContine(String text) {
        return u -> contains(u.getUsername(), text);
    }

    /**
     * @param text the text to look for (case insensitive)
     * @return a predicate true for the users whose first name contains the text
     */
    public static Predicate<Utilizator> prenumeContine(String text) {
        return u -> contains(u.getFirstName(), text);
    }

    /**
     * @param text the text to look for (case insensitive)
     * @return a predicate true for the users whose last name contains the text
     */
    public static Predicate<Utilizator> numeContine(String text) {
        return u -> contains(u.getLastName(), text);
    }

    /**
     * Combination of criteria with {@code or}.
     *
     * @param text the text to look for (case insensitive)
     * @return a predicate true for the users whose username, first name or last name
     *         contains the text
     */
    public static Predicate<Utilizator> contine(String text) {
        return usernameContine(text).or(prenumeContine(text)).or(numeContine(text));
    }

    /**
     * @param moment a moment in time
     * @return a predicate true for the accounts created after the given moment
     */
    public static Predicate<Utilizator> creatDupa(LocalDateTime moment) {
        return u -> u.getCreatedAt().isAfter(moment);
    }

    /**
     * Uses {@code negate}: the opposite of {@link #creatDupa(LocalDateTime)}.
     *
     * @param moment a moment in time
     * @return a predicate true for the accounts created at or before the given moment
     */
    public static Predicate<Utilizator> creatPanaLa(LocalDateTime moment) {
        return creatDupa(moment).negate();
    }

    /**
     * @param idsWithFriends the ids of the users that have at least one friend
     * @return a predicate true for the users that have at least one friend
     */
    public static Predicate<Utilizator> cuPrieteni(Set<Long> idsWithFriends) {
        return u -> idsWithFriends.contains(u.getId());
    }

    /**
     * Uses {@link Predicate#not}: the opposite of {@link #cuPrieteni(Set)}.
     *
     * @param idsWithFriends the ids of the users that have at least one friend
     * @return a predicate true for the users that have no friend
     */
    public static Predicate<Utilizator> faraPrieteni(Set<Long> idsWithFriends) {
        return Predicate.not(cuPrieteni(idsWithFriends));
    }

    private static boolean contains(String value, String text) {
        return value != null && value.toLowerCase().contains(text.toLowerCase());
    }
}
