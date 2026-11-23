package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class AuthorService {

    private final AuthorDao authors;

    public AuthorService(AuthorDao authors) {
        this.authors = authors;
    }

    public List<Author> all() {
        return authors.findAllWithBooks().stream()
                .map(AuthorEntity::toAuthor)
                .toList();
    }

    public List<AuthorStats> stats() {
        return authors.stats();
    }

    public List<Book> books(long authorId) {
        AuthorEntity author = authors.findById(authorId)
                .orElseThrow(() -> new NotFoundException("Author " + authorId + " not found"));
        return author.books.stream()
                .map(BookEntity::toBook)
                .toList();
    }
}
