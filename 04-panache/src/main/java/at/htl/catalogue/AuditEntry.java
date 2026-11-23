package at.htl.catalogue;

import java.time.Instant;

public record AuditEntry(Instant createdAt, String text) {
}
