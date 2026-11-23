# Catalogue on a database - starter code

The Quarkus 3.40 book catalogue from the testing exercise: `GET/POST/DELETE /books`, CDI features, configuration and the full test suite.
The books still live in an in-memory list inside `BookService`. In the exercise they move into PostgreSQL, the REST API stays the same.

```bash
./mvnw quarkus:dev
curl localhost:8080/books
./mvnw test
```
