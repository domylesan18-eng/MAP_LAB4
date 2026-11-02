package org.example.ui;

import org.example.exceptions.RepositoryException;
import org.example.exceptions.ServiceException;
import org.example.exceptions.ValidationException;
import org.example.service.CriteriiUtilizator;
import org.example.service.ReteaService;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Console user interface: reads commands and calls the service. This is where the
 * exceptions of the application are caught and shown to the user.
 * <p>
 * Every command is a {@link Consumer} of its arguments, kept in a map
 * command name -&gt; action, so there is no switch over the command names.
 */
public class Console {
    private final ReteaService service;
    private final Scanner scanner = new Scanner(System.in);
    private final Map<String, Consumer<String[]>> commands = new LinkedHashMap<>();

    /**
     * @param service the service used by the commands
     */
    public Console(ReteaService service) {
        this.service = service;
        commands.put("addUser", withArgs(4, args -> {
            service.addUser(Long.parseLong(args[1]), args[2], args[3], args[4]);
            System.out.println("Utilizator adaugat.");
        }));
        commands.put("removeUser", withArgs(1, args -> System.out.println("Sters: " + service.removeUser(args[1]))));
        commands.put("addFriend", withArgs(2, args -> {
            service.addFriend(args[1], args[2]);
            System.out.println("Prietenie adaugata.");
        }));
        commands.put("removeFriend", withArgs(2, args -> {
            service.removeFriend(args[1], args[2]);
            System.out.println("Prietenie stearsa.");
        }));
        commands.put("users", args -> service.getUsers().forEach(System.out::println));
        commands.put("friends", args -> service.getFriendshipsWithUsernames().forEach(System.out::println));
        commands.put("communities", args -> System.out.println("Numar comunitati: " + service.numberOfCommunities()));
        commands.put("sociable", args ->
                System.out.println("Cea mai sociabila comunitate: " + service.mostSociableCommunity()));
        commands.put("friendsOf", withArgs(1, args -> printAll(service.friendsOf(args[1]))));
        commands.put("search", withArgs(1, args -> printAll(service.findUsers(CriteriiUtilizator.contine(args[1])))));
        commands.put("lonely", args -> printAll(service.usersWithoutFriends()));
        commands.put("top", args -> service.usersByNumberOfFriends()
                .forEach((user, count) -> System.out.println(user.getUsername() + ": " + count + " prieteni")));
        commands.put("help", args -> printHelp());
    }

    /**
     * Reads and executes commands until "exit" or the end of the input.
     */
    public void run() {
        printHelp();
        // bucla de citire de la tastatura (nu parcurge o colectie), vezi readme.txt
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) return;
            String[] args = Arrays.stream(scanner.nextLine().trim().split("\\s+"))
                    .filter(Predicate.not(String::isBlank))
                    .toArray(String[]::new);
            if (args.length == 0) continue;
            if (args[0].equals("exit")) return;
            execute(args);
        }
    }

    private void execute(String[] args) {
        try {
            Optional.ofNullable(commands.get(args[0]))
                    .ifPresentOrElse(command -> command.accept(args),
                            () -> System.out.println("Comanda necunoscuta. Scrie 'help'."));
        } catch (ValidationException e) {
            System.out.println("Date invalide: " + e.getMessage());
        } catch (ServiceException | RepositoryException e) {
            System.out.println("Eroare: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.out.println("Eroare: id-ul trebuie sa fie numar.");
        } catch (UnsupportedOperationException e) {
            System.out.println("Operatie indisponibila: " + e.getMessage());
        }
    }

    /**
     * Decorates a command with the check of its number of arguments.
     *
     * @param count the number of arguments the command needs (without the command name)
     * @param action the command, called only if there are enough arguments
     * @return a command that prints a message instead of calling {@code action}
     *         when there are not enough arguments
     */
    private static Consumer<String[]> withArgs(int count, Consumer<String[]> action) {
        return args -> {
            if (args.length - 1 >= count) action.accept(args);
            else System.out.println("Comanda " + args[0] + " are nevoie de " + count + " argumente. Scrie 'help'.");
        };
    }

    private static void printAll(List<?> items) {
        if (items.isEmpty()) System.out.println("(niciun rezultat)");
        else items.forEach(System.out::println);
    }

    private void printHelp() {
        System.out.println("""
                Comenzi:
                  addUser <id> <username> <prenume> <nume>
                  removeUser <username>
                  addFriend <username1> <username2>
                  removeFriend <username1> <username2>
                  users | friends | communities | sociable | help | exit
                  friendsOf <username> | search <text> | lonely | top""");
    }
}
