package de.iks.grocery_manager.server.model.share;

import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table
public class Share implements HasUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "share")
    private List<JoinLink> links;

    public Permissions getPermissionsFor(String user) {
        return links
            .stream()
            .filter(j -> j.getUsers().contains(user))
            .map(JoinLink::getPermissions)
            .max(Comparator.naturalOrder())
            .orElse(Permissions.NONE);
    }
}
