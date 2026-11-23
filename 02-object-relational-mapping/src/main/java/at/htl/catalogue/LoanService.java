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

    private final EntityManager em;
    private final Clock clock;

    public LoanService(EntityManager em, Clock clock) {
        this.em = em;
        this.clock = clock;
    }

    public List<Loan> all() {
        return em.createQuery("from LoanEntity", LoanEntity.class)
                .getResultStream()
                .map(LoanEntity::toLoan)
                .toList();
    }

    @Transactional
    public Loan lend(LoanRequest request) {
        BookEntity book = em.find(BookEntity.class, request.bookId());
        if (book == null) {
            throw new BookNotFoundException(request.bookId());
        }
        MemberEntity member = em.find(MemberEntity.class, request.memberId());
        if (member == null) {
            throw new NotFoundException("Member " + request.memberId() + " not found");
        }
        LoanEntity loan = new LoanEntity(book, member, LocalDate.now(clock));
        em.persist(loan);
        return loan.toLoan();
    }
}
