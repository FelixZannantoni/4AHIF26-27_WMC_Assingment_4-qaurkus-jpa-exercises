package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import java.util.List;

@ApplicationScoped
public class AuthorService {

    public List<Author> all() {
        return AuthorEntity.listWithBooks().stream()
                .map(AuthorEntity::toAuthor)
                .toList();
    }

    public List<AuthorStats> stats() {
        return AuthorEntity.stats();
    }

    public List<Book> books(long authorId) {
        AuthorEntity author = AuthorEntity.<AuthorEntity>findByIdOptional(authorId)
                .orElseThrow(() -> new NotFoundException("Author " + authorId + " not found"));
        return author.books.stream()
                .map(BookEntity::toBook)
                .toList();
    }
}
