LABORATOR 3 - Retea de socializare: fisiere, Factory, exceptii
==============================================================

Rulare: ./gradlew run (sau Main din IntelliJ, cu working directory = folderul proiectului).

Comenzi: addUser <id> <username> <prenume> <nume>, removeUser <username>,
addFriend <u1> <u2>, removeFriend <u1> <u2>, users, friends, communities, sociable, help, exit.


1. Structura
------------
domain           Entity, Utilizator, Prietenie, Tuple
validation       Validator, UtilizatorValidator, PrietenieValidator
exceptions       ValidationException, RepositoryException, ServiceException, ConfigException
repository       Repository (interfata), InMemoryRepository
repository/file  AbstractFileRepository, UtilizatorFileRepository, PrietenieFileRepository
repository/db    UtilizatorDbRepository, PrietenieDbRepository (doar schelet, pentru lab 4)
factory          RepositoryType, RepositoryFactory
service          ReteaService
ui               Console

Fisiere de date (o entitate pe linie, campuri separate prin ;):
  data/utilizatori.txt   id;prenume;nume;username;dataCrearii   ex. 1;Ana;Pop;ana;2026-09-01T10:00
  data/prietenii.txt     id1;id2;data                           ex. 1;2;2026-10-19T10:30

Data crearii contului se pune automat la addUser (momentul adaugarii) si nu poate fi in viitor.

Prietenia are ca id perechea (id1, id2), cu id-ul mai mic primul, deci (1,2) = (2,1).


2. Factory si configurare
-------------------------
RepositoryFactory (Simple Factory + Singleton) citeste src/main/resources/config.properties:

  repository.type=FILE                     MEMORY | FILE | DATABASE
  file.utilizatori=data/utilizatori.txt
  file.prietenii=data/prietenii.txt
  db.url=... db.user=... db.password=...

In functie de tip creeaza repository-urile potrivite. E singura clasa care stie de clasele
concrete; Main, service-ul si UI-ul folosesc doar interfata Repository.

Daca config-ul lipseste sau e gresit -> ConfigException si aplicatia nu porneste.
Pe DATABASE aplicatia porneste, dar operatiile afiseaza "Operatie indisponibila".

La lab 4: adaug driverul JDBC, scriu codul in *DbRepository si pun repository.type=DATABASE.
Restul ramane la fel.


3. Persistenta in fisier
------------------------
AbstractFileRepository (Template Method) tine datele in memorie si in fisier:
  - la pornire citeste fisierul;
  - dupa fiecare save/delete/update rescrie tot fisierul;
  - daca scrierea esueaza, anuleaza modificarea si din memorie.
Subclasele fac doar conversia: extractEntity (linie -> obiect) si createEntityAsString
(obiect -> linie).

Fisier lipsa: pornesc cu date goale, fisierul se creeaza la prima salvare.

Linie corupta: o afisez (fisier, numar linie, motiv) si o sar, restul se incarca normal.
O linie gresita nu trebuie sa strice toate datele. Linia sarita dispare din fisier la
urmatoarea salvare.
Fisiere de test: data/test-corupt/ (se pun caile lor in config.properties).


4. Consistenta si validare
--------------------------
  - Validarea e in repository (validatorul vine prin constructor), deci se valideaza si
    datele din fisier.
  - PrietenieValidator verifica ca ambii utilizatori exista.
  - Utilizatorii se incarca inaintea prieteniilor.
  - removeUser sterge intai prieteniile utilizatorului.
  - Service-ul verifica: username unic, deja prieteni, utilizator inexistent.


5. Exceptii
-----------
Toate cele 4 exceptii proprii sunt unchecked (extind RuntimeException).
Se prind in Console (mesaj pentru utilizator) si in Main (erori la pornire).
Fara catch gol si fara catch (Exception e).


6. SOLID si sabloane
--------------------
DIP  service-ul, UI-ul si Main depind de Repository, nu de clase concrete
SRP  fiecare clasa are un singur rol
OCP  o entitate noua in fisier = o subclasa noua, fara sa schimb clasa de baza
LSP  orice repository poate fi folosit in locul interfetei Repository

Sabloane: Simple Factory + Singleton, Template Method, Strategy (Validator),
Decorator (BufferedReader / BufferedWriter).

Simple Factory vs Abstract Factory:
  Simple   - simplu, dar un tip nou de persistenta inseamna sa modific switch-ul (incalca OCP).
  Abstract - cate o clasa factory pe tip, respecta OCP, dar sunt mai multe clase.
  Pentru 3 tipuri ajunge Simple Factory.


7. Demo
-------
  - addUser, addFriend, removeFriend, removeUser
  - exit + repornire -> datele raman
  - stergi data/prietenii.txt -> porneste gol
  - fisierele din data/test-corupt/ -> liniile gresite sunt afisate si sarite
  - repository.type=MEMORY -> datele nu raman dupa repornire
  - repository.type=DATABASE -> "Operatie indisponibila"
  - repository.type=XML -> "Aplicatia nu poate porni: ..."
