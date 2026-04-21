package de.iks.grocery_manager.server.model;

import de.iks.grocery_manager.server.model.masterdata.Product;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table
public class ShoppingList implements HasUUID, HasOwner {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean repeating;

    @ElementCollection
    @MapKeyJoinColumn(name = "product_uuid")
    @Column(name = "amount", nullable = false)
    private Map<Product, BigDecimal> products;

    @ElementCollection
    @MapKeyJoinColumn(name = "group_uuid")
    @Column(name = "amount", nullable = false)
    private Map<ProductGroup, BigDecimal> productGroups;
}
