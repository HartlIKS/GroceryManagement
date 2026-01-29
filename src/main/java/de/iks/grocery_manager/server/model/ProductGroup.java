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
public class ProductGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(nullable = false)
    private String owner;

    @Column(nullable = false)
    private String name;

    @ElementCollection
    @MapKeyJoinColumn(name = "products_uuid")
    @Column(name = "amount", nullable = false)
    private Map<Product, BigDecimal> products;
}
