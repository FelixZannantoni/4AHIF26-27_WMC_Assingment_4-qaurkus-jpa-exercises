# Catalogue ORM - starter code

The book catalogue on PostgreSQL from the exercise "Connecting to a Database": one entity `BookEntity` with an author string, `BookService` on the `EntityManager`, seed data in `import.sql`, all tests green.
In the exercise the single table grows into a real model: column mapping, an enum, a converter, authors, members and loans.

Docker or Podman must run (Dev Services starts PostgreSQL).

```bash
./mvnw quarkus:dev
curl localhost:8080/books
./mvnw test
```
