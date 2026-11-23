package at.htl.catalogue;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.time.Clock;

@ApplicationScoped
public class Clocks {

    @Produces
    @ApplicationScoped
    Clock systemClock() {
        return Clock.systemUTC();
    }
}
