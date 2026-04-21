package de.iks.grocery_manager.server.model.mdi;

import de.iks.grocery_manager.server.model.HasUUID;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table
public class ExternalAPI implements HasUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "api", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Endpoint> endpoints;

    @ElementCollection
    @CollectionTable(
        joinColumns = @JoinColumn(name = "api"),
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"api", "remote_id"}),
            @UniqueConstraint(columnNames = {"api", "local_id"})
        }
    )
    @MapKeyJoinColumn(name = "local_id")
    @Column(name = "remote_id", nullable = false)
    private Map<Product, String> productMappings;

    @ElementCollection
    @CollectionTable(
        joinColumns = @JoinColumn(name = "api"),
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"api", "remote_id"}),
            @UniqueConstraint(columnNames = {"api", "local_id"})
        }
    )
    @MapKeyJoinColumn(name = "local_id")
    @Column(name = "remote_id", nullable = false)
    private Map<Store, String> storeMappings;
}
