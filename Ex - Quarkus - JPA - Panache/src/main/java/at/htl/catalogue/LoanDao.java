package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;

@ApplicationScoped
public class LoanDao {

    private final EntityManager em;

    public LoanDao(EntityManager em) {
        this.em = em;
    }

    public List<LoanEntity> findAll() {
        return em.createQuery("select l from LoanEntity l", LoanEntity.class)
                .getResultList();
    }

    public long countOpen(long bookId) {
        return em.createQuery("""
                        select count(l) from LoanEntity l
                        where l.book.id = :bookId and l.returnDate is null""", Long.class)
                .setParameter("bookId", bookId)
                .getSingleResult();
    }

    public void add(LoanEntity loan) {
        em.persist(loan);
    }
}
