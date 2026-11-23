# Catalogue on a database - final code

The book catalogue stores its books in PostgreSQL through Hibernate ORM. `BookEntity` is the table, the record `Book` stays the REST DTO, `BookService` works with the `EntityManager`. `import.sql` seeds two books.

In dev mode and in tests Dev Services starts PostgreSQL in a container (Docker or Podman must run). Dev mode uses the fixed port 5432, tests a random one.

```bash
./mvnw quarkus:dev
curl localhost:8080/books
./mvnw test
```

The packaged application (`%prod` profile) expects an own PostgreSQL:

```bash
docker run -d --name catalogue-db -p 5432:5432 \
  -e POSTGRES_USER=app -e POSTGRES_PASSWORD=app -e POSTGRES_DB=catalogue \
  docker.io/library/postgres:18
./mvnw package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

Stop dev mode first, both want port 5432.
