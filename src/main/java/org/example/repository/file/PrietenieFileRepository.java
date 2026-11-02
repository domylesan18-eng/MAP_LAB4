package org.example.repository.file;

import org.example.domain.Prietenie;
import org.example.domain.Tuple;
import org.example.validation.Validator;

import java.time.LocalDateTime;

/**
 * Friendships in a text file, one per line: idUtilizator1;idUtilizator2;dataPrieteniei
 * (the date in ISO format, e.g. 2026-10-19T10:30).
 */
public class PrietenieFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Prietenie> {
    /**
     * @param validator the friendship validator (checks that both users exist)
     * @param fileName path of the file, e.g. data/prietenii.txt
     * @throws IllegalArgumentException if fileName is null or blank
     * @throws org.example.exceptions.RepositoryException if the file exists but cannot be read
     */
    public PrietenieFileRepository(Validator<Prietenie> validator, String fileName) {
        super(validator, fileName);
    }

    @Override
    protected Prietenie extractEntity(String[] fields) {
        checkFieldCount(fields, 3);
        return new Prietenie(Long.parseLong(fields[0].trim()), Long.parseLong(fields[1].trim()),
                LocalDateTime.parse(fields[2].trim()));
    }

    @Override
    protected String createEntityAsString(Prietenie p) {
        return p.getIdUser1() + SEPARATOR + p.getIdUser2() + SEPARATOR + p.getFriendsFrom();
    }
}
