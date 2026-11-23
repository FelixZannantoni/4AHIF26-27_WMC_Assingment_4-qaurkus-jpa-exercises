package at.htl.catalogue;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record LoanRequest(@NotNull Long bookId, @NotNull UUID memberId) {
}
