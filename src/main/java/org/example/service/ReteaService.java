package org.example.service;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.domain.Utilizator;
import org.example.exceptions.ServiceException;
import org.example.repository.Repository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Business logic of the social network: users, friendships and communities.
 * Depends only on the {@link Repository} abstraction, not on the kind of persistence.
 */
public class ReteaService {
    private final Repository<Long, Utilizator> userRepo;
    private final Repository<Tuple<Long, Long>, Prietenie> friendRepo;

    /**
     * @param userRepo the user repository
     * @param friendRepo the friendship repository
     */
    public ReteaService(Repository<Long, Utilizator> userRepo, Repository<Tuple<Long, Long>, Prietenie> friendRepo) {
        this.userRepo = userRepo;
        this.friendRepo = friendRepo;
    }

    /**
     * Adds a new user; the account creation date is the current moment.
     *
     * @param id id of the user
     * @param username unique username
     * @param firstName first name
     * @param lastName last name
     * @throws ServiceException if the username is already used
     * @throws org.example.exceptions.ValidationException if the user is not valid
     * @throws org.example.exceptions.RepositoryException if the id is already used or saving fails
     */
    public void addUser(Long id, String username, String firstName, String lastName) {
        if (findByUsername(username).isPresent())
            throw new ServiceException("Username-ul '" + username + "' este deja folosit!");
        userRepo.save(new Utilizator(id, username, firstName, lastName,
                LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
    }

    /**
     * Removes a user together with all his friendships.
     *
     * @param username the username of the user
     * @return the removed user
     * @throws ServiceException if there is no user with this username
     * @throws org.example.exceptions.RepositoryException if saving the changes fails
     */
    public Utilizator removeUser(String username) {
        Long id = getByUsername(username).getId();
        // intai prieteniile, ca sa nu ramana prietenii cu un utilizator inexistent
        List<Tuple<Long, Long>> toDelete = new ArrayList<>();
        for (Prietenie p : friendRepo.findAll())
            if (p.involves(id)) toDelete.add(p.getId());
        for (Tuple<Long, Long> friendshipId : toDelete)
            friendRepo.delete(friendshipId);
        return userRepo.delete(id);
    }

    /**
     * Adds a friendship between two users, starting now.
     *
     * @param username1 the username of one user
     * @param username2 the username of the other user
     * @throws ServiceException if a user does not exist or they are already friends
     * @throws org.example.exceptions.ValidationException if the friendship is not valid
     * @throws org.example.exceptions.RepositoryException if saving fails
     */
    public void addFriend(String username1, String username2) {
        Long id1 = getByUsername(username1).getId();
        Long id2 = getByUsername(username2).getId();
        if (friendRepo.findOne(Prietenie.idOf(id1, id2)).isPresent())
            throw new ServiceException(username1 + " si " + username2 + " sunt deja prieteni!");
        friendRepo.save(new Prietenie(id1, id2, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)));
    }

    /**
     * Removes the friendship between two users.
     *
     * @param username1 the username of one user
     * @param username2 the username of the other user
     * @throws ServiceException if a user does not exist or they are not friends
     * @throws org.example.exceptions.RepositoryException if saving fails
     */
    public void removeFriend(String username1, String username2) {
        Long id1 = getByUsername(username1).getId();
        Long id2 = getByUsername(username2).getId();
        Tuple<Long, Long> friendshipId = Prietenie.idOf(id1, id2);
        if (friendRepo.findOne(friendshipId).isEmpty())
            throw new ServiceException(username1 + " si " + username2 + " nu sunt prieteni!");
        friendRepo.delete(friendshipId);
    }

    /**
     * @return all the users
     */
    public Iterable<Utilizator> getUsers() {
        return userRepo.findAll();
    }

    /**
     * @return all the friendships
     */
    public Iterable<Prietenie> getFriendships() {
        return friendRepo.findAll();
    }

    /**
     * @return the friendships as text, with usernames instead of ids
     */
    public List<String> getFriendshipsWithUsernames() {
        List<String> result = new ArrayList<>();
        for (Prietenie p : friendRepo.findAll())
            result.add(usernameOf(p.getIdUser1()) + " <-> " + usernameOf(p.getIdUser2())
                    + " (din " + p.getFriendsFrom().toLocalDate() + ")");
        return result;
    }

    /**
     * @return the number of communities (connected components of the friendship graph)
     */
    public int numberOfCommunities() {
        return getCommunities().size();
    }

    /**
     * The most sociable community is the one with the longest path in the friendship graph.
     *
     * @return the users of the most sociable community (empty if there are no users)
     */
    public List<Utilizator> mostSociableCommunity() {
        Map<Long, List<Long>> graph = buildGraph();
        List<Long> best = new ArrayList<>();
        int bestLength = -1;
        for (List<Long> community : getCommunities()) {
            int length = longestPath(community, graph);
            if (length > bestLength) {
                bestLength = length;
                best = community;
            }
        }
        List<Utilizator> result = new ArrayList<>();
        for (Long id : best) {
            Optional<Utilizator> user = userRepo.findOne(id);
            if (user.isPresent()) result.add(user.get());
        }
        return result;
    }

    private Optional<Utilizator> findByUsername(String username) {
        for (Utilizator u : userRepo.findAll())
            if (u.getUsername().equals(username)) return Optional.of(u);
        return Optional.empty();
    }

    private String usernameOf(Long id) {
        Optional<Utilizator> user = userRepo.findOne(id);
        return user.isPresent() ? user.get().getUsername() : "?" + id;
    }

    private Utilizator getByUsername(String username) {
        Optional<Utilizator> user = findByUsername(username);
        if (user.isEmpty())
            throw new ServiceException("Nu exista utilizatorul '" + username + "'");
        return user.get();
    }

    // graful ca lista de adiacenta: id user -> id-urile prietenilor
    private Map<Long, List<Long>> buildGraph() {
        Map<Long, List<Long>> graph = new HashMap<>();
        for (Utilizator u : userRepo.findAll()) graph.put(u.getId(), new ArrayList<>());
        for (Prietenie p : friendRepo.findAll()) {
            graph.get(p.getIdUser1()).add(p.getIdUser2());
            graph.get(p.getIdUser2()).add(p.getIdUser1());
        }
        return graph;
    }

    // componentele conexe, fac dfs din fiecare nod nevizitat
    private List<List<Long>> getCommunities() {
        Map<Long, List<Long>> graph = buildGraph();
        Set<Long> visited = new HashSet<>();
        List<List<Long>> communities = new ArrayList<>();
        for (Long start : graph.keySet()) {
            if (visited.contains(start)) continue;
            List<Long> component = new ArrayList<>();
            dfs(start, graph, visited, component);
            communities.add(component);
        }
        return communities;
    }

    private void dfs(Long node, Map<Long, List<Long>> graph, Set<Long> visited, List<Long> component) {
        visited.add(node);
        component.add(node);
        for (Long next : graph.get(node))
            if (!visited.contains(next)) dfs(next, graph, visited, component);
    }

    // cel mai lung drum din componenta, incerc fiecare nod ca start
    private int longestPath(List<Long> community, Map<Long, List<Long>> graph) {
        int max = 0;
        for (Long start : community)
            max = Math.max(max, longestFrom(start, graph, new HashSet<>()));
        return max;
    }

    private int longestFrom(Long node, Map<Long, List<Long>> graph, Set<Long> onPath) {
        onPath.add(node);
        int max = 0;
        for (Long next : graph.get(node))
            if (!onPath.contains(next)) max = Math.max(max, 1 + longestFrom(next, graph, onPath));
        onPath.remove(node); // backtracking
        return max;
    }
}
