LABORATOR 4 - Retea de socializare: refactorizare Java 8
=========================================================
(lambda, interfete functionale, referinte la metode, Optional, Stream API)

Rulare: ./gradlew run (sau Main din IntelliJ, cu working directory = folderul proiectului).

Comenzi: addUser <id> <username> <prenume> <nume>, removeUser <username>,
addFriend <u1> <u2>, removeFriend <u1> <u2>, users, friends, communities, sociable, help, exit.
Comenzi noi, doar pentru demonstrarea criteriilor si sortarilor:
  friendsOf <username>   prietenii unui utilizator, sortati dupa username
  search <text>          utilizatorii care contin textul in username, prenume sau nume
  lonely                 utilizatorii fara prieteni
  top                    utilizatorii dupa numarul de prieteni (descrescator, apoi username)

Aplicatia face exact ce facea la laboratorul 3: aceleasi fisiere, acelasi format,
aceleasi mesaje. Am verificat ruland acelasi scenariu de comenzi pe lab3 si pe lab4,
pe data/ si pe data/test-corupt/: iesirea si fisierele rezultate sunt identice
(singura diferenta e linia noua din help).

Istoric GIT: primul commit = codul de la lab 3, al doilea = refactorizarea.
  git diff HEAD~1 -- src     arata fiecare loc inainte / dupa.


1. Ce s-a schimbat (pe scurt)
-----------------------------
validation   Validator: @FunctionalInterface, default andThen, static of(Predicate, mesaj)
repository   Repository: default findAll(Predicate<E>)
             FileRepository<ID, E> (generic) + EntityConverters, in locul lui
             AbstractFileRepository + UtilizatorFileRepository + PrietenieFileRepository
service      CriteriiUtilizator (criterii de cautare ca Predicate<Utilizator>)
             ReteaService rescris cu stream-uri
factory      RepositoryFactory: Map<RepositoryType, Function<Validator, Repository>>, fara switch
ui           Console: Map<String, Consumer<String[]>> pentru comenzi, fara switch
util         Streams.of(Iterable) - singurul loc cu StreamSupport.stream(..., false)
domain       Prietenie.otherThan(id)


2. Tabel inainte / dupa (bucle inlocuite)
-----------------------------------------
Clasa / metoda                         Inainte (lab 3)                        Dupa (lab 4)
-------------------------------------- -------------------------------------- ------------------------------------------
ReteaService.removeUser                for peste prietenii -> lista toDelete, friendRepo.findAll(p -> p.involves(id))
                                       apoi for care sterge                   .stream().map(Prietenie::getId)
                                                                              .forEach(friendRepo::delete)
ReteaService.getFriendshipsWithUsernames for + result.add(...)                stream().map(...).collect(toList())
ReteaService.findByUsername            for + if + return Optional.of(u)       findAll(CriteriiUtilizator.areUsername(u))
                                                                              .stream().findFirst()
ReteaService.buildGraph                2 for-uri care umplu un HashMap        flatMap (fiecare prietenie -> 2 muchii)
                                                                              + groupingBy(mapping(toList()));
                                                                              toMap pentru utilizatori
ReteaService.getCommunities            for peste noduri + visited + continue  keySet().stream().map(reachableFrom)
                                                                              .distinct().collect(toList())
ReteaService.dfs                       for + if (!visited.contains)           stream().filter(not(visited::contains))
                                                                              .forEach(next -> dfs(...))
ReteaService.mostSociableCommunity     for cu bestLength/best + for care      map(c -> Tuple(c, longestPath)).max(
                                       construieste lista de utilizatori      comparingInt(Tuple::getRight)).map(...)
                                                                              .map(findOne).flatMap(Optional::stream)
                                                                              .collect(toList()).orElse(List.of())
ReteaService.longestPath               for + Math.max                         mapToInt(...).max().orElse(0)
ReteaService.longestFrom               for + add/remove pe acelasi set         filter(not(onPath::contains)).mapToInt(...)
                                       (backtracking cu efecte secundare)     .max().orElse(0); fiecare apel primeste
                                                                              un set nou (with) -> fara efecte secundare
FileRepository.loadData                while ((line = readLine()) != null)    Files.lines in try-with-resources ->
                                       cu contor lineNumber++                 lista; IntStream.range(0, n).filter(...)
                                                                              .forEach(i -> loadLine(lines.get(i), i + 1))
FileRepository.writeToFile             for + writer.write + newLine           Streams.of(findAll()).map(entityToLine)
                                                                              .collect(toList()); Files.write(path, linii)
Console.execute                        for (Utilizator u : ...) println       getUsers().forEach(System.out::println)
RepositoryFactory.create...Repository  switch (type)                          Map<RepositoryType, Function<...>>
Console.execute                        switch (args[0])                       Map<String, Consumer<String[]>>

Exemple de pipeline (pentru demonstratie):

 a) ReteaService.friendsOf
    Streams.of(friendRepo.findAll())         sursa
      .filter(p -> p.involves(id))           intermediara (lazy)
      .map(p -> p.otherThan(id))             intermediara
      .map(userRepo::findOne)                intermediara (Stream<Optional<Utilizator>>)
      .flatMap(Optional::stream)             intermediara (scoate Optional-urile goale)
      .sorted(comparing(Utilizator::getUsername))  intermediara, cu stare (are nevoie de toate elementele)
      .collect(Collectors.toList())          TERMINALA - abia acum se executa tot pipeline-ul

 b) ReteaService.buildGraph
    flatMap(p -> Stream.of(Tuple(id1,id2), Tuple(id2,id1)))   intermediara
    collect(groupingBy(Tuple::getLeft, mapping(Tuple::getRight, toList())))   terminala

 c) FileRepository.loadData
    IntStream.range(0, lines.size())         sursa (indicii, ca sa am numarul liniei)
      .filter(i -> !lines.get(i).isBlank())  intermediara
      .forEach(i -> loadLine(...))           terminala

 d) ReteaService.mostSociableCommunity: map -> max (terminala, intoarce Optional) ->
    Optional.map -> orElse.


3. Interfete functionale folosite
---------------------------------
Validator<T> (proprie)   UtilizatorValidator, PrietenieValidator; reguli noi ca lambda in Main;
                         andThen (default) si of(Predicate, mesaj) (static)
Predicate<T>             Repository.findAll(Predicate), CriteriiUtilizator (and, or, negate,
                         Predicate.not), filter-ele din service, Console (not(String::isBlank))
Function<T, R>           FileRepository (String -> E si E -> String), RepositoryFactory
                         (Validator -> Repository), Function.identity() in groupingBy/toMap
Supplier<T>              orElseThrow(() -> new ...Exception(...)) in service si factory
Consumer<T>              comenzile din Console, forEach, ifPresentOrElse
BiConsumer<K, V>         Map.forEach in comanda top
ToIntFunction / ToLongFunction   Comparator.comparingInt / comparingLong
Comparator<T>            comparing, comparingInt, comparingLong, thenComparing, reversed
BinaryOperator<T>        functia de merge din toMap
Runnable                 ramura "else" din ifPresentOrElse (comanda necunoscuta)

Referinte la metode (toate cele 4 tipuri):
  metoda statica                 EntityConverters::utilizatorFromLine, EntityConverters::prietenieToLine
                                 (in RepositoryFactory)
  metoda de instanta a unui      System.out::println, friendRepo::delete, userRepo::findOne,
  obiect anume                   visited::contains, onPath::contains
  metoda de instanta a unui      Utilizator::getUsername, Utilizator::getLastName, Prietenie::getId,
  obiect oarecare                Tuple::getLeft, String::isBlank, String::trim, Optional::stream
  constructor                    InMemoryRepository::new (in factory), LinkedHashMap::new (toMap),
                                 String[]::new (toArray in Console)

Regula de validare noua doar prin lambda (Main), fara clasa noua si fara sa modific
UtilizatorValidator:
    Validator<Utilizator> userValidator = new UtilizatorValidator()
            .andThen(u -> { if (u.getUsername().length() > 30) throw new ValidationException(...); })
            .andThen(Validator.of(u -> !RESERVED_USERNAMES.contains(...), "Username rezervat!"));
Datele existente respecta regulile, deci rezultatele raman aceleasi.

Criteriu de cautare nou doar prin lambda: in Console, de ex.
    service.findUsers(CriteriiUtilizator.contine(text).and(u -> u.getId() > 3))
sau orice Predicate<Utilizator> dat lui findUsers / findAll; nu se adauga nimic in repository.


4. Optional in Repository
-------------------------
Repository.findOne intoarce Optional<E> (inca de la lab 3) si am pastrat asa:
  - absenta rezultatului apare in semnatura, nu doar in Javadoc;
  - in service se scrie direct findOne(id).map(Utilizator::getUsername).orElse("?" + id),
    findOne(...).isPresent(), map(userRepo::findOne).flatMap(Optional::stream);
  - nu exista nicaieri verificari == null si nici get() fara verificare.
delete si update arunca RepositoryException daca entitatea nu exista (o eroare, nu un
rezultat posibil), deci intorc E, nu Optional<E>. Toate implementarile (InMemory, File,
cele doua schelete DB) si Javadoc-ul sunt consecvente.
Optional e folosit doar ca tip de retur, nu pentru campuri sau parametri.


5. Template Method vs Function (repository-urile pe fisier)
-----------------------------------------------------------
Am trecut la compozitie: un singur FileRepository<ID, E> care primeste prin constructor
Function<String, E> si Function<E, String>. Conversiile sunt metode statice in
EntityConverters si se dau ca referinte la metode din RepositoryFactory.
Motive:
  - nu mai e nevoie de cate o subclasa pentru fiecare entitate (3 clase -> 1 clasa + metode);
  - la Template Method, constructorul clasei de baza apela metoda abstracta extractEntity
    inainte ca subclasa sa fie initializata (capcana din Effective Java, Item 19, notata la
    lab 3); acum functiile sunt campuri setate inainte de loadData(), deci problema dispare;
  - conversiile se pot testa separat si se pot inlocui fara mostenire.
FileRepository extinde in continuare InMemoryRepository (pastrarea in memorie si
validarea sunt aceleasi); s-a inlocuit doar partea de conversie linie <-> entitate.


6. Fisiere si exceptii
----------------------
  - Citire: Files.lines(path) in try-with-resources (stream-ul tine fisierul deschis), adunat
    intr-o lista, apoi IntStream.range pe indici ca sa raportez linia corupta cu numarul ei.
  - Scriere: Files.write(path, linii), liniile obtinute printr-un stream peste entitati.
    Files.write pune separatorul de linie dupa fiecare linie, la fel ca newLine() din lab 3.
  - IOException (checked) -> RepositoryException. Eroarea de citire aparuta in timpul
    parcurgerii stream-ului vine ca UncheckedIOException si se impacheteaza tot in
    RepositoryException. Lambda-urile nu arunca exceptii checked.
  - Fisier lipsa: repo gol, se creeaza la primul save. Linie corupta: raportata si sarita.
    Formatul fisierelor nu s-a schimbat.
  - DATABASE: scheletele arunca in continuare UnsupportedOperationException (lab 5).


7. Bucle pastrate
-----------------
Console.run: while (true) care citeste comenzi de la tastatura. Nu parcurge o colectie
(numarul de iteratii depinde de utilizator, nu de date), iar varianta cu
Stream.generate(...).takeWhile(...) e mai greu de citit si amesteca afisarea prompt-ului
cu stream-ul. In rest, in domain, service, repository si ui nu mai exista for/while.

Recursivitatea din dfs si longestFrom (backtracking pentru cel mai lung drum) a ramas
recursivitate, dar vecinii se proceseaza cu stream-uri. longestFrom nu mai modifica
multimea primita: fiecare apel primeste o multime noua (with), deci functiile din
filter / mapToInt nu au efecte secundare.


8. Demo
-------
  - aceleasi comenzi ca la lab 3 -> aceleasi rezultate (users, friends, communities, sociable,
    addUser, addFriend, removeFriend, removeUser), datele raman dupa repornire
  - data/test-corupt/ (copiati config.properties de acolo) -> aceleasi linii raportate
  - repository.type=MEMORY / DATABASE / XML -> la fel ca la lab 3
  - addUser 60 admin A B -> "Username rezervat!" (regula noua, lambda)
  - search an / friendsOf mihai / lonely / top -> criterii (Predicate) si sortari (Comparator)
  - git diff HEAD~1 -- src/main/java/org/example/service/ReteaService.java -> inainte / dupa
