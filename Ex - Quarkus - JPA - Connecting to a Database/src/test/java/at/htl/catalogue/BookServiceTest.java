package at.htl.catalogue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.enterprise.event.Event;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookServiceTest {

    private BookService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        Notifier silent = text -> { };
        Event<BookCreated> events = Mockito.mock(Event.class);
        // a fresh service with fresh fakes for every test
        service = new BookService(silent, events, new TestConfig());
    }

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
        service.add(new Book(null, "Dune", "Herbert", 1965));
        service.add(new Book(null, "Solaris", "Lem", 1961));

        List<Book> result = service.find("lem");

        assertEquals(1, result.size());
        assertEquals("Solaris", result.get(0).title());
    }

    private static class TestConfig implements CatalogueConfig {
        @Override
        public int maxResults() {
            return 10;
        }

        @Override
        public boolean audit() {
            return false;
        }
    }
}
