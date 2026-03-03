package de.iks.grocery_manager.server.model.share;

import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table
public class JoinLink implements HasUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne(cascade = {
        CascadeType.DETACH,
        CascadeType.MERGE,
        CascadeType.REFRESH,
        CascadeType.PERSIST,
    })
    @JoinColumn(nullable = false)
    private Share share;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Permissions permissions;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private boolean singleUse;

    private Instant validTo;

    @ElementCollection
    @Column(nullable = false)
    private Set<String> users;

    public JoinLink use(String user) {
        if(!active) return null;
        if(validTo != null && validTo.isBefore(Instant.now())) return null;
        users.add(user);
        if(singleUse) active = false;
        return this;
    }
}
