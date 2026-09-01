package de.iks.grocery_manager.server.model.masterdata;

import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table
public class Product implements HasUUID {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Version
    @Column(nullable = false)
    private int version;

    @Column(nullable = false)
    private String name;

    private String EAN;

    private String image;
}