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

    private String owner;

    private String name;

    @ManyToMany
    @JoinTable
    @MapKey
    private Map<UUID, Product> products;
}
