package at.htl.catalogue;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IsbnTest {

    private final IsbnConverter converter = new IsbnConverter();

    @Test
    void invalidIsbnIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Isbn("123"));
    }

    @Test
    void converterWritesTheDigits() {
        assertEquals("9780441013593", converter.convertToDatabaseColumn(new Isbn("9780441013593")));
        assertEquals(new Isbn("9780441013593"), converter.convertToEntityAttribute("9780441013593"));
    }
}
