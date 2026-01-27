package de.iks.grocery_manager.server.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
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
    private List<Product> products;
}
