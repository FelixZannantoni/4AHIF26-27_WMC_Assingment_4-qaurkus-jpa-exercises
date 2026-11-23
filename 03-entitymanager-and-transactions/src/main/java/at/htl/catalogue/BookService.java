package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Named;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class BookService {

    private final EntityManager em;
    private final Notifier notifier;
    private final Event<BookCreated> events;
    private final CatalogueConfig config;

    public BookService(EntityManager em,
                       @Named("log") Notifier notifier,
                       Event<BookCreated> events,
                       CatalogueConfig config) {
        this.em = em;
        this.notifier = notifier;
        this.events = events;
        this.config = config;
    }

    @Timed
    public List<Book> find(String author) {
        TypedQuery<BookEntity> query = author == null
                ? em.createQuery("from BookEntity order by id", BookEntity.class)
                : em.createQuery("from BookEntity where lower(author.name) = lower(:author) order by id", BookEntity.class)
                        .setParameter("author", author);
        return query.setMaxResults(config.maxResults())
                .getResultStream()
                .map(BookEntity::toBook)
                .toList();
    }

    public Book get(long id) {
        BookEntity entity = em.find(BookEntity.class, id);
        if (entity == null) {
            throw new BookNotFoundException(id);
        }
        return entity.toBook();
    }

    @Transactional
    public Book add(Book book) {
        BookEntity entity = BookEntity.from(book);
        authorNamed(book.author()).addBook(entity);
        em.persist(entity);
        Book saved = entity.toBook();
        events.fire(new BookCreated(saved));
        notifier.send("New book: " + saved.title());
        return saved;
    }

    @Transactional
    public void remove(long id) {
        BookEntity entity = em.find(BookEntity.class, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    private AuthorEntity authorNamed(String name) {
        return em.createQuery("from AuthorEntity where name = :name", AuthorEntity.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElseGet(() -> {
                    AuthorEntity author = new AuthorEntity();
                    author.name = name;
                    em.persist(author);
                    return author;
                });
    }
}
