package at.htl.catalogue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "member")
public class MemberEntity {

    // the code creates the key: known right after new, before any persist
    @Id
    public UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 100)
    public String name;
}
