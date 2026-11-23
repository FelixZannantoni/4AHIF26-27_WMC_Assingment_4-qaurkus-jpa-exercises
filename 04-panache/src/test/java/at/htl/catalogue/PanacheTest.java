package at.htl.catalogue;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestTransaction
class PanacheTest {

    @Inject
    BookRepository books;

    @Inject
    LoanRepository loans;

    @Test
    void activeRecordFinder() {
        assertTrue(AuthorEntity.findByName("Lem").isPresent());
        assertTrue(AuthorEntity.findByName("Unknown Author").isEmpty());
    }

    @Test
    void repositoryQuery() {
        assertEquals("Dune", books.byAuthor("herbert").firstResult().title);
    }

    @Test
    void activeRecordPersist() {
        AuthorEntity author = new AuthorEntity();
        author.name = "Butler";
        author.persist();

        assertEquals(author.id, AuthorEntity.findByName("Butler").orElseThrow().id);
    }

    @Test
    void domainRepositoryCountsOpenLoans() {
        assertEquals(0, loans.openLoansOf(1L));
    }
}
