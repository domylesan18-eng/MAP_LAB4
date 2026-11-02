package org.example;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.domain.Utilizator;
import org.example.exceptions.ConfigException;
import org.example.exceptions.RepositoryException;
import org.example.exceptions.ValidationException;
import org.example.factory.RepositoryFactory;
import org.example.repository.Repository;
import org.example.service.ReteaService;
import org.example.ui.Console;
import org.example.validation.PrietenieValidator;
import org.example.validation.UtilizatorValidator;
import org.example.validation.Validator;

import java.util.Set;

/**
 * Entry point. The repositories are obtained from {@link RepositoryFactory}, so Main
 * does not know which kind of persistence is used (it is set in config.properties).
 */
public class Main {
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final Set<String> RESERVED_USERNAMES = Set.of("admin", "root", "system");

    /**
     * @param args not used
     */
    public static void main(String[] args) {
        try {
            RepositoryFactory factory = RepositoryFactory.getInstance();
            System.out.println("Persistenta: " + factory.getType());
            // reguli noi de validare, scrise ca lambda si compuse cu validatorul existent
            // (fara clase noi si fara sa modific UtilizatorValidator)
            Validator<Utilizator> userValidator = new UtilizatorValidator()
                    .andThen(u -> {
                        if (u.getUsername().length() > MAX_USERNAME_LENGTH)
                            throw new ValidationException("Username prea lung (maxim " + MAX_USERNAME_LENGTH
                                    + " caractere)!");
                    })
                    .andThen(Validator.of(u -> !RESERVED_USERNAMES.contains(u.getUsername().toLowerCase()),
                            "Username rezervat!"));
            // intai utilizatorii, ca validatorul de prietenii sa-i gaseasca
            Repository<Long, Utilizator> userRepo = factory.createUtilizatorRepository(userValidator);
            Repository<Tuple<Long, Long>, Prietenie> friendRepo =
                    factory.createPrietenieRepository(new PrietenieValidator(userRepo));
            new Console(new ReteaService(userRepo, friendRepo)).run();
        } catch (ConfigException | RepositoryException e) {
            System.err.println("Aplicatia nu poate porni: " + e.getMessage());
        }
    }
}
