package at.htl.catalogue;

import java.time.LocalDate;
import java.util.UUID;

public record Loan(Long bookId, String title, UUID memberId, String member,
                   LocalDate loanDate, LocalDate dueDate) {
}
