package at.htl.catalogue;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record Book(
        Long id,
        @NotBlank String title,
        @NotBlank String author,
        @Min(1450) int year) {
}
