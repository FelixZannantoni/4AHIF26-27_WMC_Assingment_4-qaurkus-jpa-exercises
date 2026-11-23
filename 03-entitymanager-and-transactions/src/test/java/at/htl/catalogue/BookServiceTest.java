package at.htl.catalogue;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@TestTransaction
class BookServiceTest {

    // the real service on the real (Dev Services) database,
    // every test is rolled back at the end
    @Inject
    BookService service;

    @Test
    void unknownIdThrowsNotFound() {
        assertThrows(BookNotFoundException.class, () -> service.get(42));
    }

    @Test
    void addAssignsAnId() {
        Book saved = service.add(new Book(null, "Dune", "Herbert", 1965));

        assertNotNull(saved.id());
    }

    @Test
    void findFiltersByAuthor() {
        service.add(new Book(null, "Ubik", "Dick", 1969));

        List<Book> result = service.find("dick");

        assertEquals(1, result.size());
        assertEquals("Ubik", result.get(0).title());
    }
}
