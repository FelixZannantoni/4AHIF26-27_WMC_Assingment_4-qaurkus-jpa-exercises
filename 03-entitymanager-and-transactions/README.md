# Catalogue EntityManager - starter code

The catalogue from the exercise "Object-Relational Mapping": books, authors, members and loans, mapped with JPA. The services still talk to the `EntityManager` directly and `POST /loans` lends a book twice without complaint.
In the exercise the queries move into DAOs, you write JPQL, a projection, paging, fix an N+1 problem and control transactions with `REQUIRED` and `REQUIRES_NEW`.

Docker or Podman must run (Dev Services starts PostgreSQL).

```bash
./mvnw quarkus:dev
curl localhost:8080/authors
./mvnw test
```
