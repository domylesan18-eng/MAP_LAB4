-- Laborator 5: baza de date socialnetwork
-- 1. In pgAdmin: Databases -> Create -> Database..., numele: socialnetwork
--    (sau: CREATE DATABASE socialnetwork;)
-- 2. Click dreapta pe socialnetwork -> Query Tool, deschideti acest fisier si rulati-l (F5).
-- Scriptul nu sterge nimic si poate fi rulat de mai multe ori.

-- id-ul il genereaza baza de date (GENERATED ALWAYS AS IDENTITY): nu il dam in INSERT.
-- first_name / last_name sunt coloanele cerute; username si created_at exista deja in
-- domeniul aplicatiei (Utilizator, de la lab 2) si sunt pastrate si aici (vezi readme.txt).
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR   NOT NULL,
    last_name  VARCHAR   NOT NULL,
    username   VARCHAR   NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL
);

-- username unic si fara sa tina cont de litere mari/mici (ana si Ana nu pot exista amandoi);
-- service-ul verifica doar potrivirea exacta, deci "addUser Ana X Y" ajunge la baza de date
-- si incalca acest index -> mesaj "Constrangere incalcata" in UI (demo)
CREATE UNIQUE INDEX IF NOT EXISTS users_username_ci ON users (LOWER(username));

-- o prietenie se pastreaza o singura data, cu id-ul mai mic primul (CHECK id1 < id2);
-- prieteniile unui utilizator sters dispar prin ON DELETE CASCADE (vezi readme.txt)
CREATE TABLE IF NOT EXISTS friendships (
    id1          BIGINT    REFERENCES users (id) ON DELETE CASCADE,
    id2          BIGINT    REFERENCES users (id) ON DELETE CASCADE,
    friends_from TIMESTAMP NOT NULL,
    PRIMARY KEY (id1, id2),
    CHECK (id1 < id2)
);

-- date de test (aceiasi utilizatori ca in data/utilizatori.txt), doar daca nu exista deja
INSERT INTO users (first_name, last_name, username, created_at)
SELECT v.first_name, v.last_name, v.username, v.created_at
FROM (VALUES (1, 'Ana',   'Pop',     'ana',   TIMESTAMP '2026-09-01 10:00'),
             (2, 'Mihai', 'Ionescu', 'mihai', TIMESTAMP '2026-09-03 14:20'),
             (3, 'Ioana', 'Rusu',    'ioana', TIMESTAMP '2026-09-10 08:45'),
             (4, 'Dan',   'Marin',   'dan',   TIMESTAMP '2026-09-15 19:30'),
             (5, 'Vlad',  'Stan',    'vlad',  TIMESTAMP '2026-10-01 12:00'),
             (6, 'Elena', 'Dobre',   'elena', TIMESTAMP '2026-10-02 16:10'))
         AS v(nr, first_name, last_name, username, created_at)
WHERE NOT EXISTS (SELECT 1 FROM users u WHERE u.username = v.username)
ORDER BY v.nr;   -- nr: doar ordinea inserarii, nu este id-ul

-- prieteniile de test, dupa username (id-urile sunt cele generate mai sus)
INSERT INTO friendships (id1, id2, friends_from)
SELECT LEAST(a.id, b.id), GREATEST(a.id, b.id), v.friends_from
FROM (VALUES ('ana',   'mihai', TIMESTAMP '2026-10-19 10:30'),
             ('mihai', 'ioana', TIMESTAMP '2026-10-20 12:00'),
             ('ioana', 'dan',   TIMESTAMP '2026-10-21 09:15'),
             ('vlad',  'elena', TIMESTAMP '2026-10-22 18:45'))
         AS v(u1, u2, friends_from)
JOIN users a ON a.username = v.u1
JOIN users b ON b.username = v.u2
ON CONFLICT DO NOTHING;

SELECT * FROM users ORDER BY id;
SELECT * FROM friendships ORDER BY id1, id2;
