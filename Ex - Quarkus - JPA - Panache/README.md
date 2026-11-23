# Catalogue Panache - final code

- `quarkus-hibernate-orm-panache` instead of `quarkus-hibernate-orm`
- `AuthorEntity extends PanacheEntityBase` (active record, keeps its `IDENTITY` key) with the finders `findByName`, `listWithBooks`, `stats`
- `BookRepository implements PanacheRepository<BookEntity>` replaces `BookDao`
- `PanacheQuery`: `page(...)`, `count()`, `firstResult...()`, `project(BookTitle.class)`
- domain interface `LoanRepository`, implemented by `JpaLoanRepository` on top of the Panache DAO `LoanDao`

Docker or Podman must run (Dev Services starts PostgreSQL).

```bash
./mvnw quarkus:dev
curl "localhost:8080/books?page=0&size=1"
curl "localhost:8080/books/count?author=lem"
curl localhost:8080/books/titles
./mvnw test
```
