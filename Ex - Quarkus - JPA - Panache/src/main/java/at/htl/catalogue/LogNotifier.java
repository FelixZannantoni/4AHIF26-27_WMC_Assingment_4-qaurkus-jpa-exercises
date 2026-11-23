package at.htl.catalogue;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("log")
public class LogNotifier implements Notifier {

    @Override
    public void send(String text) {
        Log.infof("LOG: %s", text);
    }
}
