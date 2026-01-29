package de.iks.grocery_manager.server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table
public class ProductGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable
    @MapKey(name = "uuid")
    private Map<UUID, Product> products;
}
