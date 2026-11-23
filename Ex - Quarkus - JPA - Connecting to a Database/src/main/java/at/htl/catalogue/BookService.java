package at.htl.catalogue;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Named;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class BookService {

    private final List<Book> books = new CopyOnWriteArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);
    private final Notifier notifier;
    private final Event<BookCreated> events;
    private final CatalogueConfig config;

    public BookService(@Named("log") Notifier notifier,
                       Event<BookCreated> events,
                       CatalogueConfig config) {
        this.notifier = notifier;
        this.events = events;
        this.config = config;
    }

    @PostConstruct
    void seed() {
        books.add(new Book(nextId.getAndIncrement(), "Dune", "Herbert", 1965));
        books.add(new Book(nextId.getAndIncrement(), "Solaris", "Lem", 1961));
    }

    @Timed
    public List<Book> find(String author) {
        return books.stream()
                .filter(b -> author == null || b.author().equalsIgnoreCase(author))
                .limit(config.maxResults())
                .toList();
    }

    public Book get(long id) {
        return books.stream()
                .filter(b -> b.id() == id)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    public Book add(Book book) {
        Book saved = book.withId(nextId.getAndIncrement());
        books.add(saved);
        events.fire(new BookCreated(saved));
        notifier.send("New book: " + saved.title());
        return saved;
    }

    public void remove(long id) {
        books.removeIf(b -> b.id() == id);
    }
}
