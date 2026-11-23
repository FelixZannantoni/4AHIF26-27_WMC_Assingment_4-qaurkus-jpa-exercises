package at.htl.catalogue;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.Clock;
import java.util.List;

@ApplicationScoped
public class AuditLog {

    private final Clock clock;
    private final CatalogueConfig config;
    private final EntityManager em;

    public AuditLog(Clock clock, CatalogueConfig config, EntityManager em) {
        this.clock = clock;
        this.config = config;
        this.em = em;
    }

    // own transaction: the entry survives a rollback of the caller
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void write(String text) {
        AuditEntity entry = new AuditEntity();
        entry.createdAt = clock.instant();
        entry.text = text;
        em.persist(entry);
    }

    public List<AuditEntry> entries() {
        return em.createQuery("select e from AuditEntity e order by e.id", AuditEntity.class)
                .getResultStream()
                .map(AuditEntity::toEntry)
                .toList();
    }

    void onCreated(@Observes BookCreated event) {
        if (config.audit()) {
            Log.infof("%s: created '%s'", clock.instant(), event.book().title());
        }
    }

    void onStart(@Observes StartupEvent ev) {
        Log.info("Catalogue is ready");
    }
}
