package at.htl.catalogue;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "author")
public class AuthorEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 100)
    public String name;

    @OneToMany(mappedBy = "author")
    public List<BookEntity> books = new ArrayList<>();

    public static Optional<AuthorEntity> findByName(String name) {
        return find("name", name).firstResultOptional();
    }

    public static List<AuthorEntity> listWithBooks() {
        return list("select distinct a from AuthorEntity a left join fetch a.books order by a.name");
    }

    public static List<AuthorStats> stats() {
        return getEntityManager().createQuery("""
                        select new at.htl.catalogue.AuthorStats(a.name, count(b))
                        from AuthorEntity a left join a.books b
                        group by a.name
                        order by a.name""", AuthorStats.class)
                .getResultList();
    }

    public void addBook(BookEntity book) {
        books.add(book);
        book.author = this;
    }

    public Author toAuthor() {
        return new Author(id, name, books.stream().map(b -> b.title).toList());
    }
}
