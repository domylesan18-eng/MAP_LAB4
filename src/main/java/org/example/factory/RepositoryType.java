package org.example.factory;

/**
 * The kind of persistence used by the repositories, read from config.properties
 * (repository.type).
 */
public enum RepositoryType {
    /** entities kept only in memory, lost when the application stops */
    MEMORY,
    /** entities kept in text files (paths: file.utilizatori, file.prietenii) */
    FILE,
    /** entities kept in a database (db.url, db.user, db.password), implemented in lab 5 */
    DATABASE
}
