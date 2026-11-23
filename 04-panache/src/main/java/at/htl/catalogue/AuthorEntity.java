package at.htl.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "author")
public class AuthorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true, length = 100)
    public String name;

    @OneToMany(mappedBy = "author")
    public List<BookEntity> books = new ArrayList<>();

    public void addBook(BookEntity book) {
        books.add(book);
        book.author = this;
    }

    public Author toAuthor() {
        return new Author(id, name, books.stream().map(b -> b.title).toList());
    }
}
