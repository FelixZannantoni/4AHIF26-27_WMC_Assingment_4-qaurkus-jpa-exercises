package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class AuthorService {

    private final EntityManager em;

    public AuthorService(EntityManager em) {
        this.em = em;
    }

    public List<Author> all() {
        return em.createQuery("from AuthorEntity order by name", AuthorEntity.class)
                .getResultStream()
                .map(AuthorEntity::toAuthor)
                .toList();
    }

    public List<Book> books(long authorId) {
        AuthorEntity author = em.find(AuthorEntity.class, authorId);
        if (author == null) {
            throw new NotFoundException("Author " + authorId + " not found");
        }
        return author.books.stream()
                .map(BookEntity::toBook)
                .toList();
    }
}
