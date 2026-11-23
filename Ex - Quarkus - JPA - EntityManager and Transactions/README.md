# Catalogue EntityManager - final code

- `BookDao`, `AuthorDao`, `LoanDao` hold every query, the services only orchestrate
- JPQL with parameters and joins, the named query `BookEntity.byGenre`, the projection `AuthorStats`
- paging with `setFirstResult` / `setMaxResults`
- `GET /authors` loads authors and books in one query (`left join fetch`); `quarkus.hibernate-orm.fetch.batch-size=-1` keeps the N+1 problem visible without it
- `LoanService.lend` runs in one transaction (`REQUIRED`): lending a lent book rolls back and answers 409
- `AuditLog.write` uses `REQUIRES_NEW`: its entry survives that rollback

Docker or Podman must run (Dev Services starts PostgreSQL).

```bash
./mvnw quarkus:dev
curl "localhost:8080/books?page=1&size=1"
curl "localhost:8080/books?genre=SCIENCE_FICTION"
curl localhost:8080/authors/stats
curl -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000001"}'
curl -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000002"}'
curl localhost:8080/audit
./mvnw test
```
