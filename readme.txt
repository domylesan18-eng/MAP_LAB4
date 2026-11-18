LABORATOR 5 - Retea de socializare: persistenta in PostgreSQL (JDBC)
====================================================================
Pornit de la solutia laboratorului 4 (cod copiat, lab 4 nu a fost modificat).

Pregatire
---------
1. pgAdmin: Databases -> Create -> Database..., numele socialnetwork.
2. Query Tool pe socialnetwork -> se ruleaza sql/socialnetwork.sql (creeaza tabelele si cativa
   utilizatori de test; poate fi rulat de mai multe ori, nu sterge nimic).
3. Se copiaza src/main/resources/config.example.properties ca config.properties (acelasi
   folder) si se completeaza db.password cu parola reala. config.properties e in .gitignore.
4. ./gradlew run   (sau Main din IntelliJ, working directory = folderul proiectului)

Comenzi: addUser <username> <prenume> <nume>, removeUser <username>, addFriend <u1> <u2>,
removeFriend <u1> <u2>, users, friends, communities, sociable, friendsOf <u>, search <text>,
lonely, top, help, exit.
Un argument cu spatii se scrie intre ghilimele: addUser ana2 "Ana Maria" Pop
addUser nu mai primeste id: aplicatia afiseaza id-ul generat de stocare.


1. Baza de date
---------------
users(id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY, first_name VARCHAR NOT NULL,
      last_name VARCHAR NOT NULL, username VARCHAR NOT NULL, created_at TIMESTAMP NOT NULL)
      + index unic pe LOWER(username)
friendships(id1 BIGINT, id2 BIGINT, friends_from TIMESTAMP NOT NULL,
            PRIMARY KEY (id1, id2), CHECK (id1 < id2),
            id1, id2 REFERENCES users(id) ON DELETE CASCADE)

Coloane in plus fata de enunt (username, created_at): domeniul aplicatiei (Utilizator) le are
inca de la laboratorul 2 si toate comenzile identifica utilizatorii dupa username. Fara ele,
datele s-ar pierde la trecerea FILE -> DATABASE si functionalitatile existente nu ar mai merge.
Coloanele cerute (id, first_name, last_name) sunt exact ca in enunt.
Indexul unic pe LOWER(username): username-ul e unic si fara diferente de litere mari/mici.
Service-ul verifica doar potrivirea exacta, deci "addUser Ana X Y" (exista "ana") trece de
service si e oprit de baza de date -> exemplu de constrangere incalcata in UI.

CHECK (id1 < id2): o prietenie e salvata o singura data. Prietenie.idOf(a, b) normalizeaza deja
perechea (id-ul mai mic primul), deci repository-ul respecta mereu constrangerea.

Prieteniile unui utilizator sters - alegerea: ON DELETE CASCADE in baza de date.
  - DELETE FROM users sterge utilizatorul si prieteniile lui intr-o singura instructiune,
    deci atomic: nu poate ramane o prietenie catre un utilizator inexistent, nici daca
    utilizatorul e sters din pgAdmin sau din alta aplicatie, nici daca programul se opreste
    intre doua operatii.
  - Fara cascada, stergerea utilizatorului ar esua cu eroare de cheie straina daca service-ul
    nu a sters inainte prieteniile.
  Service-ul (ReteaService.removeUser) sterge in continuare explicit prieteniile, pentru ca
  MEMORY si FILE nu au chei straine si aplicatia trebuie sa se comporte la fel pe orice stocare.
  Pe DATABASE, cascada este garantia: chiar daca o stergere de prietenie din service ar esua,
  baza de date ramane consistenta.


2. Repository-urile JDBC (repository/db)
----------------------------------------
UtilizatorDbRepository implements Repository<Long, Utilizator>
PrietenieDbRepository  implements Repository<Tuple<Long, Long>, Prietenie>
  - Conexiune: DriverManager.getConnection(url, user, password), cu parametrii primiti de la
    RepositoryFactory (din config.properties). Fiecare metoda deschide Connection,
    PreparedStatement si ResultSet in try-with-resources (se inchid si la exceptie).
    Constructorul nu se conecteaza, deci aplicatia porneste si cu serverul oprit; fiecare
    comanda incearca din nou (dupa pornirea serverului merge fara repornirea aplicatiei).
  - Toate interogarile sunt PreparedStatement cu ?; valorile se dau cu setString/setLong/
    setTimestamp si nu se concateneaza niciodata in SQL (textele SQL sunt constante).
  - Id generat: INSERT fara id, prepareStatement(sql, Statement.RETURN_GENERATED_KEYS),
    apoi getGeneratedKeys() -> entity.setId(...). save cu un id deja setat ->
    IllegalArgumentException (coloana e GENERATED ALWAYS).
  - delete: DELETE ... RETURNING intoarce randul sters in aceeasi instructiune (fara SELECT
    separat, deci fara fereastra intre citire si stergere).

Contractul Repository (schimbat fata de lab 4, ca in curs/seminar):
  findOne(id)  -> Optional<E>, gol daca nu exista
  save(e)      -> Optional.empty() daca s-a salvat, Optional.of(e) daca id-ul exista deja
  delete(id)   -> Optional cu entitatea stearsa, gol daca nu exista
  update(e)    -> Optional.empty() daca s-a modificat, Optional.of(e) daca id-ul nu exista;
                  la DB se decide dupa executeUpdate() (0 randuri modificate)
  argument null (sau id null) -> IllegalArgumentException
  validarea se face inainte de save si update (validator primit prin constructor)
La lab 4 delete/update aruncau RepositoryException pentru id inexistent; acum lipsa entitatii
este un rezultat (Optional), iar RepositoryException ramane doar pentru erori reale de
stocare. InMemoryRepository si FileRepository au fost aduse la acelasi contract, iar service-ul
transforma Optional-ul in ServiceException ("nu sunt prieteni", "nu exista utilizatorul").

Id-ul il genereaza stocarea si la MEMORY / FILE: InMemoryRepository primeste optional un
generator Function<Collection<ID>, ID> (pentru utilizatori: cel mai mare id + 1), deci
service-ul nu mai alege id-ul pe niciun tip de stocare.


3. Factory si configurare
-------------------------
config.properties: repository.type (DATABASE / FILE / MEMORY), file.utilizatori, file.prietenii,
db.url, db.user, db.password. Schimbarea stocarii = doar schimbarea lui repository.type.
RepositoryFactory e singura clasa care cunoaste clasele concrete (Map<RepositoryType, Function>).
build.gradle.kts: implementation("org.postgresql:postgresql:42.7.7").
In GIT intra config.example.properties; config.properties (cu parola reala) e in .gitignore.

Regula "fara ';' in nume" era necesara doar pentru fisier (';' e separatorul de campuri). A
fost mutata din UtilizatorValidator in EntityConverters.checkNoSeparator si se adauga cu
validator.andThen(...) doar pentru FILE. Astfel numele '); DROP TABLE users; -- poate fi
salvat in baza de date, iar fisierele raman corecte.


4. Exceptii
-----------
SQLException (checked) -> RepositoryException (unchecked), cu SQLException ca si cauza.
SqlErrors.wrap alege mesajul dupa SQLState (coduri standard JDBC):
  08xxx           baza de date nu este disponibila (server oprit / db.url gresit)
  28P01, 28000    utilizator sau parola gresita
  3D000           baza de date nu exista
  42P01           tabelele lipsesc (rulati sql/socialnetwork.sql)
  23505           constrangere UNIQUE incalcata
  23503           cheie straina incalcata
  23514           CHECK incalcat
  23502           NOT NULL incalcat
Console prinde separat ValidationException, ServiceException si RepositoryException si
afiseaza doar mesajul: fara stack trace, fara catch gol, fara catch (Exception e).
Main prinde ConfigException | RepositoryException la pornire.


5. Demonstratie
---------------
  - addUser ion Ion Pop          -> "Utilizator adaugat, id generat: N"; in pgAdmin
                                    SELECT * FROM users apare randul cu id-ul N
  - addFriend ion ana, friends, removeFriend ion ana, removeUser ion -> verificat in pgAdmin
  - exit si pornire din nou      -> users arata aceleasi date (raman in baza de date)
  - repository.type=FILE / MEMORY in config.properties -> aceleasi comenzi, alta stocare
  - serverul oprit (Services -> postgresql-x64-18 -> Stop) -> users:
      "Eroare la stocarea datelor: Baza de date nu este disponibila: serverul PostgreSQL
       este oprit ..."; dupa Start, aceeasi comanda merge fara repornirea aplicatiei
  - addUser Ana X Y (exista ana) -> "Constrangere incalcata: exista deja o inregistrare cu
                                    aceeasi valoare unica - ... users_username_ci"
  - addUser bobby "Robert'); DROP TABLE users; --" Tables
      -> salvat ca text obisnuit: users il afiseaza, tabelul users exista in continuare
         (valoarea e parametru al PreparedStatement, nu face parte din textul SQL)

Am verificat toate punctele de mai sus pe un server PostgreSQL 18 de test.
