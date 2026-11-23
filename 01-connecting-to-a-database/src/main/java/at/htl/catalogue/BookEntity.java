package at.htl.catalogue;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "book")
public class BookEntity {

    @Id
    @GeneratedValue
    public Long id;
    public String title;
    public String author;
    public int year;

    public Book toBook() {
        return new Book(id, title, author, year);
    }

    public static BookEntity from(Book book) {
        BookEntity entity = new BookEntity();
        entity.title = book.title();
        entity.author = book.author();
        entity.year = book.year();
        return entity;
    }
}
