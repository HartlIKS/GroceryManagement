package de.iks.grocery_manager.server.model.mdi;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class ProductEndpoint extends Endpoint {
    @Column(nullable = false)
    private String productIdPath;

    private String productNamePath;

    private String productImagePath;

    private String productEANPath;
}
