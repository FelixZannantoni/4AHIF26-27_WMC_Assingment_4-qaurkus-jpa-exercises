package at.htl.catalogue;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class BookService {

    private final BookRepository books;
    private final Notifier notifier;
    private final Event<BookCreated> events;
    private final CatalogueConfig config;

    public BookService(BookRepository books,
                       @Named("log") Notifier notifier,
                       Event<BookCreated> events,
                       CatalogueConfig config) {
        this.books = books;
        this.notifier = notifier;
        this.events = events;
        this.config = config;
    }

    @Timed
    public List<Book> find(String author, int page, int size) {
        int max = Math.min(size, config.maxResults());
        return query(author)
                .page(Page.of(page, max))
                .list()
                .stream()
                .map(BookEntity::toBook)
                .toList();
    }

    public long count(String author) {
        return query(author).count();
    }

    public List<BookTitle> titles() {
        return books.findAll(Sort.by("title"))
                .project(BookTitle.class)
                .list();
    }

    public List<Book> findByGenre(Genre genre) {
        return books.byGenre(genre).stream()
                .map(BookEntity::toBook)
                .toList();
    }

    public Book get(long id) {
        return books.findByIdOptional(id)
                .map(BookEntity::toBook)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book add(Book book) {
        BookEntity entity = BookEntity.from(book);
        authorNamed(book.author()).addBook(entity);
        books.persist(entity);
        Book saved = entity.toBook();
        events.fire(new BookCreated(saved));
        notifier.send("New book: " + saved.title());
        return saved;
    }

    @Transactional
    public void remove(long id) {
        books.deleteById(id);
    }

    private PanacheQuery<BookEntity> query(String author) {
        return author == null
                ? books.findAll(Sort.by("id"))
                : books.byAuthor(author);
    }

    private AuthorEntity authorNamed(String name) {
        return AuthorEntity.findByName(name)
                .orElseGet(() -> {
                    AuthorEntity author = new AuthorEntity();
                    author.name = name;
                    author.persist();
                    return author;
                });
    }
}
