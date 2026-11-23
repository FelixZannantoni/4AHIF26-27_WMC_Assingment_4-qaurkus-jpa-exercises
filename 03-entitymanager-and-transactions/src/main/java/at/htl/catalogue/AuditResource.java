package at.htl.catalogue;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/audit")
@Produces(MediaType.APPLICATION_JSON)
public class AuditResource {

    private final AuditLog auditLog;

    public AuditResource(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    @GET
    public List<AuditEntry> list() {
        return auditLog.entries();
    }
}
