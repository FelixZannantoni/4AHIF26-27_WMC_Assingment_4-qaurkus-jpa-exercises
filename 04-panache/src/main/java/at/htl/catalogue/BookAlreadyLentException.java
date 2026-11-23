package at.htl.catalogue;

public class BookAlreadyLentException extends RuntimeException {

    public BookAlreadyLentException(long bookId) {
        super("Book " + bookId + " is already lent");
    }
}
