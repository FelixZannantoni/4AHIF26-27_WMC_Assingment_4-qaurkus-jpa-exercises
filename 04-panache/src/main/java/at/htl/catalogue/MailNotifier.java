package at.htl.catalogue;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("mail")
public class MailNotifier implements Notifier {

    @Override
    public void send(String text) {
        Log.infof("MAIL: %s", text);
    }
}
