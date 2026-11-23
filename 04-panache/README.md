# Catalogue Panache - starter code

The catalogue from the exercise "EntityManager and Transactions": DAOs on the `EntityManager`, JPQL, paging, a projection, `join fetch`, a loan use case with `REQUIRED` and an audit with `REQUIRES_NEW`.
In the exercise Panache replaces the hand-written DAOs: active record for authors, a `PanacheRepository` for books, `PanacheQuery` for paging and projections, and a domain repository for loans.

Docker or Podman must run (Dev Services starts PostgreSQL).

```bash
./mvnw quarkus:dev
curl localhost:8080/books
./mvnw test
```
