package at.htl.catalogue;

import java.util.List;

// the domain's view of loans: no Panache, no JPA in the signatures
public interface LoanRepository {

    List<LoanEntity> all();

    long openLoansOf(long bookId);

    void add(LoanEntity loan);
}
