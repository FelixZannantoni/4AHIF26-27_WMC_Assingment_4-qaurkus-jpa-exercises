package at.htl.catalogue;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record Book(
        Long id,
        @NotBlank String title,
        @NotBlank String author,
        @Min(1450) int year,
        Genre genre,
        @Pattern(regexp = "\\d{13}") String isbn) {

    // the short form used before genre and isbn existed
    public Book(Long id, String title, String author, int year) {
        this(id, title, author, year, null, null);
    }
}
