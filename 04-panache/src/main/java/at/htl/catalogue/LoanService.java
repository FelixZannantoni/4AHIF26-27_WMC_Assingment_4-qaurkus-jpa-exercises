package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class LoanService {

    private final LoanRepository loans;
    private final BookRepository books;
    private final EntityManager em;
    private final AuditLog auditLog;
    private final Clock clock;

    public LoanService(LoanRepository loans, BookRepository books, EntityManager em,
                       AuditLog auditLog, Clock clock) {
        this.loans = loans;
        this.books = books;
        this.em = em;
        this.auditLog = auditLog;
        this.clock = clock;
    }

    public List<Loan> all() {
        return loans.all().stream()
                .map(LoanEntity::toLoan)
                .toList();
    }

    @Transactional
    public Loan lend(LoanRequest request) {
        auditLog.write("lend book " + request.bookId() + " to member " + request.memberId());

        BookEntity book = books.findByIdOptional(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));
        MemberEntity member = em.find(MemberEntity.class, request.memberId());
        if (member == null) {
            throw new NotFoundException("Member " + request.memberId() + " not found");
        }
        LoanEntity loan = new LoanEntity(book, member, LocalDate.now(clock));
        loans.add(loan);

        // the query flushes the insert above, so the new loan is counted too
        if (loans.openLoansOf(book.id) > 1) {
            throw new BookAlreadyLentException(book.id);   // rollback: the insert is undone
        }
        return loan.toLoan();
    }
}
