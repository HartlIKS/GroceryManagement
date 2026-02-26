package de.iks.grocery_manager.server.model.masterdata;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table
public class PriceListing {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Product product;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Store store;
    @Column(nullable = false)
    private Instant validFrom;
    @Column(nullable = false)
    private Instant validTo;

    @Column(nullable = false)
    private BigDecimal price;
}
