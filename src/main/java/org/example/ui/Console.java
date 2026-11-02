package org.example.ui;

import org.example.domain.Utilizator;
import org.example.exceptions.RepositoryException;
import org.example.exceptions.ServiceException;
import org.example.exceptions.ValidationException;
import org.example.service.ReteaService;

import java.util.Scanner;

/**
 * Console user interface: reads commands and calls the service. This is where the
 * exceptions of the application are caught and shown to the user.
 */
public class Console {
    private final ReteaService service;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * @param service the service used by the commands
     */
    public Console(ReteaService service) {
        this.service = service;
    }

    /**
     * Reads and executes commands until "exit" or the end of the input.
     */
    public void run() {
        printHelp();
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) return;
            String[] args = scanner.nextLine().trim().split("\\s+");
            if (args[0].equals("exit")) return;
            try {
                execute(args);
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
    }

    private void execute(String[] args) {
        switch (args[0]) {
            case "addUser":
                if (!hasArgs(args, 4)) return;
                service.addUser(Long.parseLong(args[1]), args[2], args[3], args[4]);
                System.out.println("Utilizator adaugat.");
                return;
            case "removeUser":
                if (!hasArgs(args, 1)) return;
                System.out.println("Sters: " + service.removeUser(args[1]));
                return;
            case "addFriend":
                if (!hasArgs(args, 2)) return;
                service.addFriend(args[1], args[2]);
                System.out.println("Prietenie adaugata.");
                return;
            case "removeFriend":
                if (!hasArgs(args, 2)) return;
                service.removeFriend(args[1], args[2]);
                System.out.println("Prietenie stearsa.");
                return;
            case "users":
                for (Utilizator u : service.getUsers()) System.out.println(u);
                return;
            case "friends":
                for (String line : service.getFriendshipsWithUsernames()) System.out.println(line);
                return;
            case "communities":
                System.out.println("Numar comunitati: " + service.numberOfCommunities());
                return;
            case "sociable":
                System.out.println("Cea mai sociabila comunitate: " + service.mostSociableCommunity());
                return;
            case "help":
                printHelp();
                return;
            case "":
                return;
            default:
                System.out.println("Comanda necunoscuta. Scrie 'help'.");
        }
    }

    private boolean hasArgs(String[] args, int count) {
        if (args.length - 1 >= count) return true;
        System.out.println("Comanda " + args[0] + " are nevoie de " + count + " argumente. Scrie 'help'.");
        return false;
    }

    private void printHelp() {
        System.out.println("""
                Comenzi:
                  addUser <id> <username> <prenume> <nume>
                  removeUser <username>
                  addFriend <username1> <username2>
                  removeFriend <username1> <username2>
                  users | friends | communities | sociable | help | exit""");
    }
}
