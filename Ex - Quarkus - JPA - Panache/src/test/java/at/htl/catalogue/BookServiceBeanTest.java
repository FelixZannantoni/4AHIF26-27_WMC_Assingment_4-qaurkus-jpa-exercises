package at.htl.catalogue;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class BookServiceBeanTest {

    @Inject
    BookService service;

    @Test
    void addAssignsAnId() {
        Book saved = service.add(new Book(null, "Hyperion", "Simmons", 1989));

        assertNotNull(saved.id());
    }
}
