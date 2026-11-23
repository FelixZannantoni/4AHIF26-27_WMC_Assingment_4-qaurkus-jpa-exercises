package at.htl.catalogue;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class BookRepository implements PanacheRepository<BookEntity> {

    public PanacheQuery<BookEntity> byAuthor(String name) {
        return find("lower(author.name) = lower(?1)", Sort.by("id"), name);
    }

    public List<BookEntity> byGenre(Genre genre) {
        return list("#BookEntity.byGenre", Parameters.with("genre", genre));
    }
}
