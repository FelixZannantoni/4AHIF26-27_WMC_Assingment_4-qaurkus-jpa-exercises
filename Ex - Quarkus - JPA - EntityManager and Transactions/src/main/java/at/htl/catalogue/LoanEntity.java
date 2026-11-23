package at.htl.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "loan")
public class LoanEntity {

    @EmbeddedId
    public LoanId id;

    @ManyToOne
    @MapsId("bookId")
    public BookEntity book;

    @ManyToOne
    @MapsId("memberId")
    public MemberEntity member;

    @Column(name = "due_date", nullable = false)
    public LocalDate dueDate;

    @Column(name = "return_date")
    public LocalDate returnDate;   // null while the book is out

    protected LoanEntity() {
    }

    public LoanEntity(BookEntity book, MemberEntity member, LocalDate loanDate) {
        this.id = new LoanId(book.id, member.id, loanDate);
        this.book = book;
        this.member = member;
        this.dueDate = loanDate.plusWeeks(4);
    }

    public Loan toLoan() {
        return new Loan(book.id, book.title, member.id, member.name, id.loanDate, dueDate);
    }
}
