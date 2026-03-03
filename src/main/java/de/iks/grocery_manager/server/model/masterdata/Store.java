package de.iks.grocery_manager.server.model.masterdata;

import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Currency;
import java.util.UUID;

@Data
@Entity
@Table
public class Store implements HasUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;
    @Column(nullable = false)
    private String name;
    private Address address = new Address();
    private String logo;
    @Column(nullable = false)
    private Currency currency;
}
