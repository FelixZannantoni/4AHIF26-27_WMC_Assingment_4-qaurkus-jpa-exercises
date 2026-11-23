package at.htl.catalogue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookValidationTest {

    private final Validator validator = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    @ParameterizedTest
    @CsvSource({"'',Herbert,1965", "Dune,'',1965", "Dune,Herbert,12"})
    void invalidBooksViolate(String title, String author, int year) {
        Set<ConstraintViolation<Book>> violations =
                validator.validate(new Book(null, title, author, year));

        assertFalse(violations.isEmpty());
    }

    @Test
    void validBookHasNoViolations() {
        Set<ConstraintViolation<Book>> violations =
                validator.validate(new Book(null, "Dune", "Herbert", 1965));

        assertTrue(violations.isEmpty());
    }
}
