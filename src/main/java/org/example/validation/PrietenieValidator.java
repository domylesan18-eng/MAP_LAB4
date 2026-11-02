package org.example.validation;

import org.example.domain.Prietenie;
import org.example.domain.Utilizator;
import org.example.exceptions.ValidationException;
import org.example.repository.Repository;

/**
 * Validator for friendships: both users must exist, a user cannot be friend with
 * himself and the date is required. Because the friendship repository validates on
 * every save, also when loading from the file, a friendship can only exist between
 * existing users.
 */
public class PrietenieValidator implements Validator<Prietenie> {
    private final Repository<Long, Utilizator> userRepo;

    /**
     * @param userRepo the repository used to check that both users exist
     */
    public PrietenieValidator(Repository<Long, Utilizator> userRepo) {
        this.userRepo = userRepo;
    }

    /**
     * @param p the friendship to validate
     * @throws ValidationException if the friendship is not valid
     */
    @Override
    public void validate(Prietenie p) {
        Long id1 = p.getIdUser1();
        Long id2 = p.getIdUser2();
        if (id1 == null || id2 == null)
            throw new ValidationException("Id-urile utilizatorilor nu pot fi nule!");
        if (id1.equals(id2))
            throw new ValidationException("Un utilizator nu poate fi prieten cu el insusi!");
        if (p.getFriendsFrom() == null)
            throw new ValidationException("Data prieteniei lipseste!");
        if (userRepo.findOne(id1).isEmpty())
            throw new ValidationException("Nu exista utilizatorul cu id-ul " + id1);
        if (userRepo.findOne(id2).isEmpty())
            throw new ValidationException("Nu exista utilizatorul cu id-ul " + id2);
    }
}
