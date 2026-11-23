package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class BookService {

    private final BookDao books;
    private final AuthorDao authors;
    private final Notifier notifier;
    private final Event<BookCreated> events;
    private final CatalogueConfig config;

    public BookService(BookDao books,
                       AuthorDao authors,
                       @Named("log") Notifier notifier,
                       Event<BookCreated> events,
                       CatalogueConfig config) {
        this.books = books;
        this.authors = authors;
        this.notifier = notifier;
        this.events = events;
        this.config = config;
    }

    @Timed
    public List<Book> find(String author, int page, int size) {
        int max = Math.min(size, config.maxResults());
        int first = page * max;
        List<BookEntity> result = author == null
                ? books.findAll(first, max)
                : books.findByAuthor(author, first, max);
        return result.stream()
                .map(BookEntity::toBook)
                .toList();
    }

    public List<Book> findByGenre(Genre genre) {
        return books.findByGenre(genre).stream()
                .map(BookEntity::toBook)
                .toList();
    }

    public Book get(long id) {
        return books.findById(id)
                .map(BookEntity::toBook)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Transactional
    public Book add(Book book) {
        BookEntity entity = BookEntity.from(book);
        authorNamed(book.author()).addBook(entity);
        books.add(entity);
        Book saved = entity.toBook();
        events.fire(new BookCreated(saved));
        notifier.send("New book: " + saved.title());
        return saved;
    }

    @Transactional
    public void remove(long id) {
        books.findById(id).ifPresent(books::remove);
    }

    private AuthorEntity authorNamed(String name) {
        return authors.findByName(name)
                .orElseGet(() -> {
                    AuthorEntity author = new AuthorEntity();
                    author.name = name;
                    authors.add(author);
                    return author;
                });
    }
}
