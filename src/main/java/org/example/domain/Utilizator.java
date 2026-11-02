package org.example.domain;

import java.time.LocalDateTime;

/**
 * A user of the social network.
 */
public class Utilizator extends Entity<Long> {
    private final String username;
    private final String firstName;
    private final String lastName;
    private final LocalDateTime createdAt;

    /**
     * @param id id of the user
     * @param username unique name of the user
     * @param firstName first name
     * @param lastName last name
     * @param createdAt the moment the account was created
     */
    public Utilizator(Long id, String username, String firstName, String lastName, LocalDateTime createdAt) {
        setId(id);
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    /**
     * @return the unique username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return the moment the account was created
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return getId() + ": " + username + " (" + firstName + " " + lastName + "), cont creat pe "
                + createdAt.toLocalDate();
    }
}
