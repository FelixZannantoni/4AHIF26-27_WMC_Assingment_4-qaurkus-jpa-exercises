# Catalogue ORM - final code

The catalogue with a real model behind the unchanged `/books` API:

- `BookEntity` with column mapping, `Genre` stored as `STRING`, `Isbn` value type with an auto-applied `AttributeConverter`
- `AuthorEntity` (`IDENTITY` key) with `@ManyToOne` from book and `@OneToMany(mappedBy)` back, kept in sync by `addBook`
- `MemberEntity` with a UUID key created in Java
- `LoanEntity` with the composite key `LoanId` (`@EmbeddedId` + `@MapsId`)
- DTO records `Book`, `Author`, `Loan`, `LoanRequest` - entities are never serialized

Docker or Podman must run (Dev Services starts PostgreSQL).

```bash
./mvnw quarkus:dev
curl localhost:8080/authors
curl localhost:8080/authors/1/books
curl -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000001"}'
./mvnw test
```
