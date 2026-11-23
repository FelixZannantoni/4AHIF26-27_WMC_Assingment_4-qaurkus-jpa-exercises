package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BookDao {

    private final EntityManager em;

    public BookDao(EntityManager em) {
        this.em = em;
    }

    public Optional<BookEntity> findById(long id) {
        return Optional.ofNullable(em.find(BookEntity.class, id));
    }

    public List<BookEntity> findAll(int first, int max) {
        return em.createQuery("select b from BookEntity b order by b.id", BookEntity.class)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    public List<BookEntity> findByAuthor(String name, int first, int max) {
        return em.createQuery("""
                        select b from BookEntity b
                        join b.author a
                        where lower(a.name) = lower(:name)
                        order by b.id""", BookEntity.class)
                .setParameter("name", name)
                .setFirstResult(first)
                .setMaxResults(max)
                .getResultList();
    }

    public List<BookEntity> findByGenre(Genre genre) {
        return em.createNamedQuery("BookEntity.byGenre", BookEntity.class)
                .setParameter("genre", genre)
                .getResultList();
    }

    public void add(BookEntity book) {
        em.persist(book);
    }

    public void remove(BookEntity book) {
        em.remove(book);
    }
}
