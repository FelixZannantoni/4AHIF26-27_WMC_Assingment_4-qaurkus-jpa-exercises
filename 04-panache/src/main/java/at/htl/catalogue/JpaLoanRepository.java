package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
class JpaLoanRepository implements LoanRepository {

    private final LoanDao dao;

    JpaLoanRepository(LoanDao dao) {
        this.dao = dao;
    }

    @Override
    public List<LoanEntity> all() {
        return dao.listAll();
    }

    @Override
    public long openLoansOf(long bookId) {
        return dao.count("book.id = ?1 and returnDate is null", bookId);
    }

    @Override
    public void add(LoanEntity loan) {
        dao.persist(loan);
    }
}
