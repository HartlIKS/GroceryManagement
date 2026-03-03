package de.iks.grocery_manager.server.model;

import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Entity
@Table
public class ShoppingTrip implements HasUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    @Column(nullable = false)
    private String owner;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Store store;
    @Column(nullable = false)
    private Instant time;

    @ElementCollection
    @MapKeyJoinColumn(name = "product_uuid")
    @Column(name = "amount", nullable = false)
    private Map<Product, BigDecimal> products;
}
