package at.htl.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "book")
public class BookEntity {

    @Id
    @GeneratedValue
    public Long id;

    @Column(nullable = false, length = 200)
    public String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id")
    public AuthorEntity author;

    @Column(name = "published_year", nullable = false)
    public int year;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    public Genre genre;

    @Column(length = 13)
    public Isbn isbn;

    public Book toBook() {
        return new Book(id, title, author.name, year, genre,
                isbn == null ? null : isbn.value());
    }

    public static BookEntity from(Book book) {
        BookEntity entity = new BookEntity();
        entity.title = book.title();
        entity.year = book.year();
        entity.genre = book.genre();
        entity.isbn = book.isbn() == null ? null : new Isbn(book.isbn());
        return entity;
    }
}
