package org.example.domain;

import java.time.LocalDateTime;

/**
 * Friendship between two users. The id is the pair of user ids, normalized so that
 * the smaller id comes first: friendship (1, 2) and friendship (2, 1) are the same.
 */
public class Prietenie extends Entity<Tuple<Long, Long>> {
    private final LocalDateTime friendsFrom;

    /**
     * @param idUser1 id of one user
     * @param idUser2 id of the other user
     * @param friendsFrom the moment the users became friends
     */
    public Prietenie(Long idUser1, Long idUser2, LocalDateTime friendsFrom) {
        setId(idOf(idUser1, idUser2));
        this.friendsFrom = friendsFrom;
    }

    /**
     * Builds the normalized id of the friendship between two users (smaller id first).
     *
     * @param a id of one user
     * @param b id of the other user
     * @return the friendship id, the same for (a, b) and (b, a)
     */
    public static Tuple<Long, Long> idOf(Long a, Long b) {
        if (a != null && b != null && a > b) return new Tuple<>(b, a);
        return new Tuple<>(a, b);
    }

    /**
     * @return the smaller of the two user ids
     */
    public Long getIdUser1() {
        return getId().getLeft();
    }

    /**
     * @return the larger of the two user ids
     */
    public Long getIdUser2() {
        return getId().getRight();
    }

    /**
     * @return the moment the users became friends
     */
    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    /**
     * @param userId id of a user
     * @return true if the user is one of the two friends
     */
    public boolean involves(Long userId) {
        return getIdUser1().equals(userId) || getIdUser2().equals(userId);
    }

    @Override
    public String toString() {
        return getIdUser1() + " <-> " + getIdUser2() + " (din " + friendsFrom + ")";
    }
}
