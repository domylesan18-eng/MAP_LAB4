package org.example;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.domain.Utilizator;
import org.example.exceptions.ConfigException;
import org.example.exceptions.RepositoryException;
import org.example.factory.RepositoryFactory;
import org.example.repository.Repository;
import org.example.service.ReteaService;
import org.example.ui.Console;
import org.example.validation.PrietenieValidator;
import org.example.validation.UtilizatorValidator;

/**
 * Entry point. The repositories are obtained from {@link RepositoryFactory}, so Main
 * does not know which kind of persistence is used (it is set in config.properties).
 */
public class Main {
    /**
     * @param args not used
     */
    public static void main(String[] args) {
        try {
            RepositoryFactory factory = RepositoryFactory.getInstance();
            System.out.println("Persistenta: " + factory.getType());
            // intai utilizatorii, ca validatorul de prietenii sa-i gaseasca
            Repository<Long, Utilizator> userRepo = factory.createUtilizatorRepository(new UtilizatorValidator());
            Repository<Tuple<Long, Long>, Prietenie> friendRepo =
                    factory.createPrietenieRepository(new PrietenieValidator(userRepo));
            new Console(new ReteaService(userRepo, friendRepo)).run();
        } catch (ConfigException | RepositoryException e) {
            System.err.println("Aplicatia nu poate porni: " + e.getMessage());
        }
    }
}
