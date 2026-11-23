package at.htl.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class LoanId implements Serializable {

    public Long bookId;
    public UUID memberId;
    @Column(name = "loan_date")
    public LocalDate loanDate;

    protected LoanId() {
    }

    public LoanId(Long bookId, UUID memberId, LocalDate loanDate) {
        this.bookId = bookId;
        this.memberId = memberId;
        this.loanDate = loanDate;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LoanId other
                && Objects.equals(bookId, other.bookId)
                && Objects.equals(memberId, other.memberId)
                && Objects.equals(loanDate, other.loanDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bookId, memberId, loanDate);
    }
}
