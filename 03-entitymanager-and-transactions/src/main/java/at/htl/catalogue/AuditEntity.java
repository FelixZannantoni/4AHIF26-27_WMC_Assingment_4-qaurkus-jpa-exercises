package at.htl.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_entry")
public class AuditEntity {

    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "created_at", nullable = false)
    public Instant createdAt;

    @Column(nullable = false)
    public String text;

    public AuditEntry toEntry() {
        return new AuditEntry(createdAt, text);
    }
}
