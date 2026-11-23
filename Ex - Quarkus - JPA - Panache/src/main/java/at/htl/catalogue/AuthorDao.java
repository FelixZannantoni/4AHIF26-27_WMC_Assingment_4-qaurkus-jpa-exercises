package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AuthorDao {

    private final EntityManager em;

    public AuthorDao(EntityManager em) {
        this.em = em;
    }

    public Optional<AuthorEntity> findById(long id) {
        return Optional.ofNullable(em.find(AuthorEntity.class, id));
    }

    public Optional<AuthorEntity> findByName(String name) {
        return em.createQuery("select a from AuthorEntity a where a.name = :name", AuthorEntity.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public List<AuthorEntity> findAllWithBooks() {
        return em.createQuery("""
                        select distinct a from AuthorEntity a
                        left join fetch a.books
                        order by a.name""", AuthorEntity.class)
                .getResultList();
    }

    public List<AuthorStats> stats() {
        return em.createQuery("""
                        select new at.htl.catalogue.AuthorStats(a.name, count(b))
                        from AuthorEntity a left join a.books b
                        group by a.name
                        order by a.name""", AuthorStats.class)
                .getResultList();
    }

    public void add(AuthorEntity author) {
        em.persist(author);
    }
}
