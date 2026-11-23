# Ex - Quarkus - JPA - EntityManager and Transactions

##fragment

> From a catalogue that lends one book twice to a catalogue that says no. 🔁
> Move every query into DAOs, write JPQL, a projection and paging, kill an N+1 problem in the SQL log, and let one transaction decide whether a loan exists - while the audit entry survives the rollback

### 🙋 A quick survey first
Before we type a single line, we collect.

##fragment

> - Where in the catalogue is SQL sent today - and how many classes call the `EntityManager` directly? ##fragment
> - Two pupils borrow the same book at the same moment. What should the second one get back? ##fragment
> - The loan fails. Should anybody still be able to see that somebody **tried**? ##fragment

##fragment
- 👉 Write down your answer to the second question as an HTTP status code

### 🎯 What we build today
##side-by-side-start
#### The catalogue at the end
- `BookDao`, `AuthorDao`, `LoanDao`: every query lives there, the services only orchestrate
- `GET /books?genre=...` with a named query, `?page=&size=` with paging
- `GET /authors/stats` with a DTO projection
- `GET /authors` with titles, in **one** `select`

##separator
##fragment

#### The use case that must be atomic
- `POST /loans` for a lent book answers `409` and leaves **no** loan behind
- `GET /audit` still shows the failed attempt
- 5 new tests, 35 green in total

##side-by-side-end
##fragment

> [!note]
> The theory is in [[Quarkus - JPA - EntityManager and Transactions]]. This exercise applies it to the catalogue from [[Ex - Quarkus - JPA - Object-Relational Mapping]]: books, authors, members and loans on PostgreSQL. The next step, [[Quarkus - JPA - Panache]], does the same work with less code.

### 👀 The finished thing
This is what your catalogue answers at the end of the lesson:

##fragment

```bash
curl "localhost:8080/books?genre=SCIENCE_FICTION"
curl localhost:8080/authors/stats
curl -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000002"}'
curl localhost:8080/audit
```

##fragment
- Dune and Solaris, then `[{"name":"Herbert","bookCount":1},{"name":"Lem","bookCount":1}]`
- If book 1 is already lent: `409` with `{"message":"Book 1 is already lent"}`
- `/audit` lists that attempt anyway: it was written in its **own** transaction

### 🕐 Agenda: 100 minutes
| Step | Minutes | Content                                         | You end the step with                                  |
| ---- | ------- | ----------------------------------------------- | ------------------------------------------------------ |
| 0    | 8       | Clone, run, find the bug                        | Book 1 is lent to Ada **and** Alan                     |
| 1    | 18      | `BookDao` and `AuthorDao`                       | `BookService` has no `EntityManager` any more          |
| 2    | 10      | JPQL join and a named query                     | `?genre=SCIENCE_FICTION` returns Dune and Solaris      |
| 3    | 8       | DTO projection                                  | `GET /authors/stats` counts books per author           |
| 4    | 8       | Paging                                          | `?page=1&size=1` returns only Solaris                  |
| 5    | 14      | N+1 in the log, cured with `join fetch`         | `GET /authors` sends one `select` instead of three     |
| 6    | 14      | The loan use case in one transaction            | Lending a lent book answers `409`, nothing is stored   |
| 7    | 12      | `REQUIRES_NEW` for the audit                    | `GET /audit` shows the rolled-back attempt             |
| 8    | 8       | Tests (copy from `final-code`)                  | `./mvnw test`: 35 tests, all green                     |

##fragment
- Each step ends with a **checkpoint** ⏰: stop there until the class is with you

### 🧰 Setup: clone the starter
```bash
git clone https://github.com/HTL-LEO-WMC/wmc4-qaurkus-jpa-exercises.git
cd "wmc4-qaurkus-jpa-exercises/Ex - Quarkus - JPA - EntityManager and Transactions"
```

##fragment
- Repository: [https://github.com/HTL-LEO-WMC/wmc4-qaurkus-jpa-exercises](https://github.com/HTL-LEO-WMC/wmc4-qaurkus-jpa-exercises) - branch `main` is the starter, branch [`final_code`](https://github.com/HTL-LEO-WMC/wmc4-qaurkus-jpa-exercises/tree/final_code) is the `final-code` we compare with
- Open the folder in **IntelliJ IDEA Ultimate**, let it import the Maven project
- Quarkus 3.40, Java 25 or newer, **Docker or Podman must run**: Dev Services starts PostgreSQL
- Look at `BookService`, `AuthorService`, `LoanService`: all three talk to the `EntityManager` directly

## 🚀 Step 0: Run it and find the bug
> First we see what works - and what must not work.

### ▶️ Start dev mode
```bash
./mvnw quarkus:dev
curl localhost:8080/books
curl localhost:8080/authors
curl localhost:8080/loans
```

##fragment
- Expected: Dune (id 1) and Solaris (id 2), the authors Herbert and Lem, and an empty list `[]` of loans
- The data comes from `import.sql`; the members are Ada (`...0001`) and Alan (`...0002`)
- `quarkus.hibernate-orm.log.sql=true` is on: every `select` appears in the terminal. Keep it visible all lesson

### 🐞 Lend one book twice
```bash
curl -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000001"}'
curl -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000002"}'
curl localhost:8080/loans
```

##fragment
- Both answer `201`: Dune is now lent to Ada **and** to Alan
- The primary key of a loan is (book, member, date), so two different members never collide
- 👉 This is the bug we fix in step 6. After your next code change, dev mode restarts and `drop-and-create` resets the data

### ⏰ Everybody running?
> `curl localhost:8080/books` returns two books, and you saw Dune lent twice

## 🗂️ Step 1: DAOs
> One class per entity holds the queries, the service holds the use case.

### 🧱 `BookDao`: find and change
Create `BookDao` next to `BookService`:

##fragment

##side-by-side-start
```java
@ApplicationScoped
public class BookDao {

    private final EntityManager em;

    public BookDao(EntityManager em) {
        this.em = em;
    }

    public Optional<BookEntity> findById(long id) {
        return Optional.ofNullable(em.find(BookEntity.class, id));
    }

    public void add(BookEntity book) {
        em.persist(book);
    }

    public void remove(BookEntity book) {
        em.remove(book);
    }
}
```

##separator
##fragment

- A **DAO** (data access object) wraps the `EntityManager` for one entity, exactly like the `StudentDao` in the theory deck
- `findById` returns an `Optional`: the caller decides what "not found" means - a `404`, an empty list, a new object
- The DAO has **no** `@Transactional`: the transaction boundary belongs to the service that knows the use case

##side-by-side-end

### 🔍 `BookDao`: the two lists
Add these methods to `BookDao`:

##fragment

##side-by-side-start
```java
    public List<BookEntity> findAll(int max) {
        return em.createQuery("select b from BookEntity b order by b.id", BookEntity.class)
                .setMaxResults(max)
                .getResultList();
    }

    public List<BookEntity> findByAuthor(String name, int max) {
        return em.createQuery("""
                        select b from BookEntity b
                        where lower(b.author.name) = lower(:name)
                        order by b.id""", BookEntity.class)
                .setParameter("name", name)
                .setMaxResults(max)
                .getResultList();
    }
```

##separator
##fragment

- The two queries come from `BookService.find`, written out with the alias `b`
- `:name` is a **named parameter**: never glue user input into the JPQL string
- `max` is still `config.maxResults()`; in step 4 these signatures get a `first` for paging

##side-by-side-end

### 🧱 `AuthorDao`
Create `AuthorDao`:

##fragment

##side-by-side-start
```java
@ApplicationScoped
public class AuthorDao {

    private final EntityManager em;

    public AuthorDao(EntityManager em) {
        this.em = em;
    }

    public Optional<AuthorEntity> findById(long id) {
        return Optional.ofNullable(em.find(AuthorEntity.class, id));
    }

    public Optional<AuthorEntity> findByName(String name) {
        return em.createQuery("select a from AuthorEntity a where a.name = :name", AuthorEntity.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public void add(AuthorEntity author) {
        em.persist(author);
    }
}
```

##separator
##fragment

- `findByName` is the query `BookService` used to find or create the author of a new book
- `getResultStream().findFirst()` turns "zero or one row" into an `Optional` without an exception
- Imports for both DAOs: `jakarta.enterprise.context.ApplicationScoped`, `jakarta.persistence.EntityManager`, `java.util.*`

##side-by-side-end

### ⏰ Two DAOs compile?
> `BookDao` with 5 methods, `AuthorDao` with 3. Nothing uses them yet - that is next

### ✂️ `BookService`: swap the dependency
In `BookService`, replace the field `em` and its constructor parameter:

##fragment

##side-by-side-start
```java
    private final BookDao books;
    private final AuthorDao authors;
```

```java
    public BookService(BookDao books,
                       AuthorDao authors,
                       @Named("log") Notifier notifier,
                       Event<BookCreated> events,
                       CatalogueConfig config) {
        this.books = books;
        this.authors = authors;
```

##separator
##fragment

- Constructor injection as before: CDI finds both DAOs because they are `@ApplicationScoped`
- Delete the imports of `EntityManager` and `TypedQuery`: if IntelliJ still finds a use of `em`, that line is the next one to change

##side-by-side-end

### ✂️ `BookService`: use the DAOs
Replace the bodies of `find`, `get`, `remove` and `authorNamed`, and the `persist` calls in `add`:

##fragment

##side-by-side-start
```java
        List<BookEntity> result = author == null
                ? books.findAll(config.maxResults())
                : books.findByAuthor(author, config.maxResults());
        return result.stream()
                .map(BookEntity::toBook)
                .toList();
```

```java
        return books.findById(id)
                .map(BookEntity::toBook)
                .orElseThrow(() -> new BookNotFoundException(id));
```

##separator
##fragment

```java
        books.findById(id).ifPresent(books::remove);
```

```java
        return authors.findByName(name)
                .orElseGet(() -> {
                    AuthorEntity author = new AuthorEntity();
                    author.name = name;
                    authors.add(author);
                    return author;
                });
```

- In `add`: `books.add(entity)` instead of `em.persist(entity)`

##side-by-side-end
##fragment
- `@Transactional` stays on `add` and `remove` in the service: one use case, one transaction, no matter how many DAO calls
- `orElseGet` runs the lambda only when the author is missing - `orElse` would create one every time

### ✅ Verify: nothing changed
```bash
curl localhost:8080/books
curl "localhost:8080/books?author=lem"
curl -X POST localhost:8080/books -H "Content-Type: application/json" \
     -d '{"title":"Ubik","author":"Dick","year":1969}'
```

##fragment
- Same two books, then only Solaris, then `201` with id `100` and a new author Dick
- The SQL log shows the same statements as before: a DAO changes **where** the query lives, not what it sends
- 👉 Search `BookService` for `em.`: there must be no hit

### ⏰ `BookService` without `EntityManager`?
> All three curls answer as expected. If not, compare `BookService` with `final-code` - `find` and `findByGenre` differ there, they come in steps 2 and 4

## 🔗 Step 2: JPQL join and a named query
> Make the join visible, and give a fixed query a name.

### 🔗 An explicit join
In `BookDao.findByAuthor`, replace the query text:

##fragment

##side-by-side-start
```java
                        select b from BookEntity b
                        join b.author a
                        where lower(a.name) = lower(:name)
                        order by b.id""", BookEntity.class)
```

##separator
##fragment

- `b.author.name` was an **implicit** join; `join b.author a` says the same thing out loud and gives the author an alias
- With an alias you can filter, sort or count on the author without repeating the path
- 👀 Compare the SQL in the log before and after: both versions join the table `author`, the generated SQL is the same

##side-by-side-end

### 📋 A named query
Put this annotation on `BookEntity`, below `@Table`:

##fragment

```java
@NamedQuery(name = "BookEntity.byGenre",
            query = "select b from BookEntity b where b.genre = :genre order by b.title")
```

##fragment
- A named query is checked **at startup**: a typo in it stops the application instead of failing at the first request
- The name convention `Entity.purpose` keeps names unique across the persistence unit
- Import `jakarta.persistence.NamedQuery`

### 🧩 Use it in DAO, service and resource
##side-by-side-start
```java
    public List<BookEntity> findByGenre(Genre genre) {
        return em.createNamedQuery("BookEntity.byGenre", BookEntity.class)
                .setParameter("genre", genre)
                .getResultList();
    }
```

```java
    public List<Book> findByGenre(Genre genre) {
        return books.findByGenre(genre).stream()
                .map(BookEntity::toBook)
                .toList();
    }
```

##separator
##fragment

- The first method goes into `BookDao`, the second into `BookService`
- The parameter is the `Genre` **enum**, not a string: Hibernate binds it as `'SCIENCE_FICTION'` because of `@Enumerated(EnumType.STRING)`
- In `BookResource.list`, add the parameter `@QueryParam("genre") Genre genre` and, as first line:

```java
        if (genre != null) {
            return service.findByGenre(genre);
        }
```

##side-by-side-end

### ✅ Verify
```bash
curl "localhost:8080/books?genre=SCIENCE_FICTION"
curl "localhost:8080/books?author=lem"
```

##fragment
- First: Dune, then Solaris (sorted by title). Second: only Solaris
- If both `genre` and `author` are given, `genre` wins - one simple rule, written in the resource
- 👉 Try `?genre=FANTASY`: what comes back, and how many `select`s do you see?

### ⏰ Genre filter works?
> `?genre=SCIENCE_FICTION` returns two books, `?author=lem` one

## 🎯 Step 3: A DTO projection
> When you only need numbers, do not load entities at all.

### 🧾 The record and the query
Create the record `AuthorStats`, then add `stats()` to `AuthorDao`:

##fragment

##side-by-side-start
```java
public record AuthorStats(String name, long bookCount) {
}
```

```java
    public List<AuthorStats> stats() {
        return em.createQuery("""
                        select new at.htl.catalogue.AuthorStats(a.name, count(b))
                        from AuthorEntity a left join a.books b
                        group by a.name
                        order by a.name""", AuthorStats.class)
                .getResultList();
    }
```

##separator
##fragment

- `select new` calls the constructor of the record for every row - the **fully qualified** name is required
- `count(b)` is a `long`, so the record component must be `long` (or `Long`)
- `left join`: an author without books still appears, with `bookCount` 0
- The result is no entity: it is not managed, not dirty-checked, never written back

##side-by-side-end

### 🌐 Service and endpoint
##side-by-side-start
```java
    public List<AuthorStats> stats() {
        return authors.stats();
    }
```

```java
    @GET
    @Path("/stats")
    public List<AuthorStats> stats() {
        return service.stats();
    }
```

##separator
##fragment

- The first method goes into `AuthorService`, the second into `AuthorResource`
- `AuthorService` needs the `AuthorDao` for this: add it as a field and constructor parameter **next to** the `EntityManager` for now - step 5 removes the `EntityManager`

##side-by-side-end

### ✅ Verify
```bash
curl localhost:8080/authors/stats
```

##fragment
- Expected: `[{"name":"Herbert","bookCount":1},{"name":"Lem","bookCount":1}]`
- The log shows **one** `select` with `count(...)` and `group by` - the database counts, not Java
- 👉 Add a book by Lem with `POST /books` and call it again

### ⏰ Stats are counted?
> `/authors/stats` returns two objects with `bookCount`

## 📄 Step 4: Paging
> Never hand out a whole table: give the client one page at a time.

### 📄 The DAO takes a `first`
In `BookDao`, give `findAll` and `findByAuthor` a parameter `int first` and set it on the query:

##fragment

##side-by-side-start
```java
    public List<BookEntity> findAll(int first, int max) {
        return em.createQuery("select b from BookEntity b order by b.id", BookEntity.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }
```

##separator
##fragment

- `setFirstResult` skips rows, `setMaxResults` limits them: the SQL gets an `offset` and a `fetch first` clause
- Paging needs a stable `order by` - without it the database may return a different order on every page
- `findByAuthor(String name, int first, int max)` gets the same `.setFirstResult(first)` line

##side-by-side-end

### 🧮 Service and resource
##side-by-side-start
```java
    public List<Book> find(String author, int page, int size) {
        int max = Math.min(size, config.maxResults());
        int first = page * max;
        List<BookEntity> result = author == null
                ? books.findAll(first, max)
                : books.findByAuthor(author, first, max);
```

```java
    public List<Book> list(@QueryParam("author") String author,
                           @QueryParam("genre") Genre genre,
                           @QueryParam("page") @DefaultValue("0") int page,
                           @QueryParam("size") @DefaultValue("20") int size) {
```

##separator
##fragment

- `config.maxResults()` is now an **upper limit**: in dev mode it is 5 (`%dev.catalogue.max-results`), so `size=100` still gives at most 5
- Pages count from 0; `first` is computed from the page and the **effective** size
- In `BookResource`, the last line becomes `return service.find(author, page, size);`
- In `BookServiceTest`, the call becomes `service.find("dick", 0, 10)` - otherwise the tests do not compile

##side-by-side-end

### ✅ Verify
```bash
curl "localhost:8080/books?page=0&size=1"
curl "localhost:8080/books?page=1&size=1"
curl "localhost:8080/books?page=2&size=1"
```

##fragment
- Dune, then Solaris, then `[]`
- 👀 The log shows the `offset` and `fetch first` clause, the values are bound as parameters
- 👉 Add four books with `POST /books`, then call `?size=100`: how many come back in dev mode, and why?

### ⏰ Paging works?
> `?page=1&size=1` returns only Solaris, and `BookServiceTest` compiles again

## 💣 Step 5: N+1 in the log
> First we make it happen, then we make it go away.

### 📚 Authors with their titles
The record `Author` gets the titles of the author's books:

##fragment

##side-by-side-start
```java
public record Author(Long id, String name, List<String> titles) {
```

```java
    public Author toAuthor() {
        return new Author(id, name, books.stream().map(b -> b.title).toList());
    }
```

##separator
##fragment

- The first is `Author.java`, the second replaces `toAuthor` in `AuthorEntity`
- `books` is a `@OneToMany`: **lazy** by default, so every access to it may cost a `select`
- Import `java.util.List` in `Author`

##side-by-side-end

### 🛠️ Switch off the safety net
Add to `application.properties`:

##fragment

```ini
# Quarkus loads lazy collections in batches of 16 by default - off, so N+1 shows in the log
quarkus.hibernate-orm.fetch.batch-size=-1
```

##fragment
- Quarkus switches **batch fetching** on by default: it loads the books of 16 authors in one `where author_id = any(?)`
- That hides the N+1 problem - it is the `@BatchSize` cure from the theory deck, applied everywhere
- Today we want to **see** the problem, so we turn it off

### 💣 The naive query
In `AuthorService`, write `all()` like this for now - the `EntityManager` is still there from step 3:

##fragment

##side-by-side-start
```java
    public List<Author> all() {
        return em.createQuery("select a from AuthorEntity a order by a.name", AuthorEntity.class)
                .getResultStream()
                .map(AuthorEntity::toAuthor)
                .toList();
    }
```

##separator
##fragment

- This is the **before** state, we replace it in two slides
- While you are in the class: `books(authorId)` now uses `authors.findById(authorId).orElseThrow(...)` with the same `NotFoundException` as before
- Call `curl localhost:8080/authors` and look at the log

##side-by-side-end

### 🔎 Count the selects
```log
select a1_0.id,a1_0.name from author a1_0 order by a1_0.name
select b1_0.author_id,b1_0.id,... from book b1_0 where b1_0.author_id=?
select b1_0.author_id,b1_0.id,... from book b1_0 where b1_0.author_id=?
```

##fragment
- Shortened: your log prints each statement over several lines
- **1** query for the authors, **N** queries for the books of each author: 1 + 2 here, 1 + 3000 in a real library
- `toAuthor()` touches `books` once per author, each touch is a `select`
- 👉 How many selects do you expect after `POST /books` with a third author?

### 🩹 Cure it with `join fetch`
Add to `AuthorDao`:

##fragment

##side-by-side-start
```java
    public List<AuthorEntity> findAllWithBooks() {
        return em.createQuery("""
                        select distinct a from AuthorEntity a
                        left join fetch a.books
                        order by a.name""", AuthorEntity.class)
                .getResultList();
    }
```

```java
        return authors.findAllWithBooks().stream()
```

##separator
##fragment

- `join fetch` loads authors **and** their books in one SQL statement and fills the collections
- `left`: authors without books stay in the result; `distinct`: one author object per author, not one per book row
- The second line replaces the start of `AuthorService.all()`; now remove the `EntityManager` field, constructor parameter and import from `AuthorService`

##side-by-side-end

### ✅ Verify
```bash
curl localhost:8080/authors
```

##fragment
- Expected: `[{"id":1,"name":"Herbert","titles":["Dune"]},{"id":2,"name":"Lem","titles":["Solaris"]}]`
- The log shows **one** `select ... from author a1_0 left join book b1_0 on a1_0.id=b1_0.author_id`
- The **use case** decides what to load: `GET /authors/{id}/books` does not need a fetch join

### 💭 And the `LazyInitializationException`?
- In the theory deck, a lazy collection read **after** the persistence context closed throws `LazyInitializationException`
- Here it does not happen: for a read without `@Transactional`, Quarkus keeps the session open for the whole request
- ⚠️ Do not rely on that: load what the use case needs **in the query**, as we just did

### ⏰ One select for all authors?
> `/authors` shows the titles, the log shows one `select` with `left join`. `AuthorService` has no `EntityManager` any more

## 🤝 Step 6: One transaction for the loan
> Lending is one use case: either the loan is stored and valid, or nothing is stored.

### 🚫 The exception and its status
Create `BookAlreadyLentException` and add a second mapper to `ErrorHandlers`:

##fragment

##side-by-side-start
```java
public class BookAlreadyLentException extends RuntimeException {

    public BookAlreadyLentException(long bookId) {
        super("Book " + bookId + " is already lent");
    }
}
```

```java
    @ServerExceptionMapper
    public RestResponse<ErrorDto> alreadyLent(BookAlreadyLentException e) {
        return RestResponse.status(Response.Status.CONFLICT, new ErrorDto(e.getMessage()));
    }
```

##separator
##fragment

- A `RuntimeException` leaving a `@Transactional` method causes a **rollback** - a checked exception would not
- `409 Conflict`: the request is fine, but it clashes with the current state of the data
- The mapper sits next to the existing `notFound` mapper, same style

##side-by-side-end

### 🧱 `LoanDao`
Create `LoanDao` with the same constructor as the other DAOs, then add:

##fragment

##side-by-side-start
```java
    public List<LoanEntity> findAll() {
        return em.createQuery("select l from LoanEntity l", LoanEntity.class)
                .getResultList();
    }

    public long countOpen(long bookId) {
        return em.createQuery("""
                        select count(l) from LoanEntity l
                        where l.book.id = :bookId and l.returnDate is null""", Long.class)
                .setParameter("bookId", bookId)
                .getSingleResult();
    }

    public void add(LoanEntity loan) {
        em.persist(loan);
    }
```

##separator
##fragment

- `countOpen` counts the loans of a book that are not returned yet - `returnDate` is `null` while the book is out
- `getSingleResult()` is safe here: `count` always returns exactly one row
- In `LoanService.all()`, use `loans.findAll().stream()` instead of the query

##side-by-side-end

### 🔒 `LoanService.lend`
Inject `LoanDao loans` and `BookDao books` into `LoanService`, then change `lend`:

##fragment

##side-by-side-start
```java
        BookEntity book = books.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));
```

```java
        loans.add(loan);

        // the query flushes the insert above, so the new loan is counted too
        if (loans.countOpen(book.id) > 1) {
            throw new BookAlreadyLentException(book.id);   // rollback: the insert is undone
        }
        return loan.toLoan();
```

##separator
##fragment

- The member is still loaded with `em.find(MemberEntity.class, ...)` - a `MemberDao` would be the same pattern again
- We **insert first, then count**: before a JPQL query on `LoanEntity`, Hibernate flushes pending inserts, so the count includes the new loan
- More than one open loan means: somebody else has it. The exception rolls back the **whole** transaction, our insert included

##side-by-side-end

### ✅ Verify: 201, then 409
```bash
curl -i -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000001"}'
curl -i -X POST localhost:8080/loans -H "Content-Type: application/json" \
     -d '{"bookId":1,"memberId":"00000000-0000-0000-0000-000000000002"}'
curl localhost:8080/loans
```

##fragment
- Ada gets `201`, Alan gets `409` with `{"message":"Book 1 is already lent"}`
- `GET /loans` lists **only** Ada's loan
- 👀 In the log of the second request: `insert into loan`, **then** `select count(...)` - the row was in the database and was rolled back

### ⏰ 409 and only one loan?
> If Alan still gets `201`: is the `if` **after** `loans.add(loan)`, and is `lend` still `@Transactional`?

## 🧾 Step 7: An audit that survives the rollback
> Every attempt to lend is recorded - also the ones that fail.

### 🗃️ Entity and record
Create `AuditEntity` and the record `AuditEntry`:

##fragment

##side-by-side-start
```java
@Entity
@Table(name = "audit_entry")
public class AuditEntity {

    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(nullable = false)
    public String text;

    public AuditEntry toEntry() {
        return new AuditEntry(createdAt, text);
    }
}
```

##separator
##fragment

```java
public record AuditEntry(Instant createdAt, String text) {
}
```

- Same pattern as books: the entity stays inside, the record goes out as JSON
- `drop-and-create` creates the table `audit_entry` at the next restart, no SQL to write
- Imports: `jakarta.persistence.*`, `java.time.Instant`

##side-by-side-end

### 🧾 `AuditLog` writes in its own transaction
The existing `AuditLog` gets an `EntityManager em` as third constructor parameter and two methods:

##fragment

##side-by-side-start
```java
    // own transaction: the entry survives a rollback of the caller
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void write(String text) {
        AuditEntity entry = new AuditEntity();
        entry.createdAt = clock.instant();
        entry.text = text;
        em.persist(entry);
    }

    public List<AuditEntry> entries() {
        return em.createQuery("select e from AuditEntity e order by e.id", AuditEntity.class)
                .getResultStream()
                .map(AuditEntity::toEntry)
                .toList();
    }
```

##separator
##fragment

- `REQUIRES_NEW` **suspends** the transaction of `lend`, starts a second one, commits it and resumes the first
- The rollback of `lend` happens later and cannot reach the committed audit row
- This works because `AuditLog` is a **different bean**: the call goes through the interceptor. `this.write(...)` inside `LoanService` would join the outer transaction

##side-by-side-end

### 🔌 Wire it in
##side-by-side-start
```java
@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
public class AuditResource {

    private final AuditLog auditLog;

    public AuditResource(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @GET
    public List<AuditEntry> list() {
        return auditLog.entries();
    }
}
```

##separator
##fragment

- Inject `AuditLog auditLog` into `LoanService` and make this the **first** line of `lend`:

```java
        auditLog.write("lend book " + request.bookId() + " to member " + request.memberId());
```

- First, because a line after the `throw` would never run for the failed attempt

##side-by-side-end

### ✅ Verify
Repeat the two `POST /loans` from step 6, then:

##fragment

```bash
curl localhost:8080/audit
```

##fragment
- Two entries: `lend book 1 to member ...0001` **and** `lend book 1 to member ...0002`
- Alan's loan is gone (`GET /loans` shows only Ada), his audit entry is not
- 👀 The log shows `insert into audit_entry` before the `select` of the book: T2 committed while T1 was still running

### ⏰ The failed attempt is audited?
> `/audit` has two entries, `/loans` one

## 🧪 Step 8: Tests
> The transaction rule is now a promise - a test keeps it. Copy these from `final-code`.

### 📋 Copy, read, run
##side-by-side-start
#### Copy from `final-code`
- `LoanTransactionTest`: new class, the whole step 6 and 7 in one test
- `AuthorResourceTest`: `statsCountBooksPerAuthor`, `authorsComeWithTheirTitles`
- `BookResourceTest`: `pagingReturnsOneBookPerPage`, `filterByGenre`

##separator
##fragment

#### Read the key lines
```java
             .body("findAll { it.bookId == " + bookId + " }", hasSize(1));
```

- `LoanTransactionTest` creates **its own** book: the test does not depend on what other tests lent
- The `findAll` GPath counts the loans of that book: one, although two inserts were sent

##side-by-side-end

### ▶️ Run all tests
Stop dev mode with `Ctrl+C` first, then:

##fragment

```bash
./mvnw test
```

##fragment
- Expected: `Tests run: 35, Failures: 0, Errors: 0`
- The tests start their own PostgreSQL container on a random port, so they do not touch your dev data
- 👉 Comment out the `throw` in `lend` and run `LoanTransactionTest` again: which assertion fails first?

### ⏰ 35 green tests?
> If a test is red, compare the class it tests with `final-code`, then call for help

## 🌍 Summary
### 📋 What you built
> - `BookDao`, `AuthorDao`, `LoanDao`: the services no longer write queries
> - JPQL with an explicit join, the named query `BookEntity.byGenre`
> - The projection `AuthorStats` and paging with `setFirstResult` / `setMaxResults`
> - An N+1 problem made visible and cured with `left join fetch`
> - `lend` as one `REQUIRED` transaction: insert, count, rollback, `409`
> - An audit in `REQUIRES_NEW` that survives the rollback

##fragment
- 👉 Compare your version with `final-code`: where did you name or order things differently?

### 🚀 Extensions for fast ones
> - **Optimistic locking**: add `@Version public long version;` to `BookEntity` and provoke an `OptimisticLockException` with two updates of the same version ##fragment
> - **Programmatic transaction**: replace the annotation on `AuditLog.write` by `QuarkusTransaction.requiringNew().run(...)` ##fragment
> - **Bulk update**: an endpoint "return all books" with one `executeUpdate()` ##fragment
> - **Lazy author**: `@ManyToOne(fetch = FetchType.LAZY)` on `BookEntity.author`, then count the selects ##fragment
> - **Quiz**: Ada lends Dune twice on the same day - why `500` and not `409`? ##fragment

##fragment
- 👉 Hints for each one follow on the next slides

### 🔐 Hints: optimistic locking
- `import.sql` inserts books without a version: add the column `version` with value `0` to both `INSERT INTO book` lines, or the start fails
- Add a `PUT /books/{id}` that takes the `version` from the client, loads the book, compares and changes the title - or write a test: load a book, change it in a `QuarkusTransaction.requiringNew()` block, then `merge` the stale copy in a second one
- Map `OptimisticLockException` to `409` with a third `@ServerExceptionMapper`
- The SQL log shows `update book set ... where id=? and version=?`: that `and version=?` is the whole trick

### ⌨️ Hints: `QuarkusTransaction` and bulk update
##side-by-side-start
#### `QuarkusTransaction`
- Remove `@Transactional(...)` from `write`
- Wrap `em.persist(entry)` in `QuarkusTransaction.requiringNew().run(() -> ...)`
- Verify: `/audit` still lists the failed attempt

##separator
##fragment

#### Bulk update
- JPQL: `update LoanEntity l set l.returnDate = :today where l.returnDate is null`
- `executeUpdate()` returns the number of rows; it needs a transaction
- ⚠️ It bypasses the persistence context: entities already loaded in the same transaction keep their old values

##side-by-side-end

### 🪝 Hints: lazy author and the quiz
##side-by-side-start
#### Lazy author
- Count the `select`s of `GET /loans` before the change: the default `EAGER` of `@ManyToOne` loads the authors of the lent books, although a `Loan` never shows an author
- After `LAZY` they are gone from `GET /loans`. `GET /books` still needs the author in `toBook()` - load it with `join fetch b.author` in `BookDao` and compare the log

##separator
##fragment

#### Quiz
- Look at `LoanId`: which three values form the primary key?
- Look at the log: which statement fails, and **before** which line of `lend`?
- 👉 How would you turn it into a `409`?

##side-by-side-end

## Questions ?
![[Pasted image 20240424074417.png]]

## References
- https://quarkus.io/guides/hibernate-orm
- https://quarkus.io/guides/transaction
- https://jakarta.ee/specifications/persistence/3.2/
- https://jakarta.ee/specifications/transactions/2.0/
- https://docs.jboss.org/hibernate/orm/7.0/userguide/html_single/Hibernate_User_Guide.html
- https://vladmihalcea.com/n-plus-1-query-problem/
- https://quarkus.io/guides/rest

## 📎 Optional deep dives and backup slides
### 🔬 Deep dive: why `insert` comes before `count`
- Hibernate's default flush mode is `AUTO`: before a JPQL query, it flushes pending changes that could affect the result
- The query touches `LoanEntity`, and there is a pending `LoanEntity` insert - so it is flushed first
- Without the flush, `countOpen` would return 1 for Alan (Ada's loan only) and the check would need `> 0` instead - and miss two parallel requests that both count 0

### 🔬 Deep dive: two requests at the same moment
- Two `lend` transactions for the same book may both insert and both count before either commits
- Under `READ COMMITTED` (the PostgreSQL default) neither sees the other's row: both answer `201`
- Real cures: a unique partial index on `loan(book_id) where return_date is null`, or a pessimistic lock on the book (`LockModeType.PESSIMISTIC_WRITE`) - see the locking slide of [[Quarkus - JPA - EntityManager and Transactions]]
