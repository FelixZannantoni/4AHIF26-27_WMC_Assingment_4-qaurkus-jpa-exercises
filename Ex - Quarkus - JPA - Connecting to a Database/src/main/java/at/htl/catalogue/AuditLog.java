package at.htl.catalogue;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.time.Clock;

@ApplicationScoped
public class AuditLog {

    private final Clock clock;
    private final CatalogueConfig config;

    public AuditLog(Clock clock, CatalogueConfig config) {
        this.clock = clock;
        this.config = config;
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
