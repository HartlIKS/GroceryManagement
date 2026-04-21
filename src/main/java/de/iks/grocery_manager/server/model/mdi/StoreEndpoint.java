package de.iks.grocery_manager.server.model.mdi;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class StoreEndpoint extends Endpoint {
    @Column(nullable = false)
    private String storeIdPath;

    private String storeNamePath;

    private String storeLogoPath;

    private String addressPath;

    private AddressPaths addressPaths = new AddressPaths();

    private String storeCurrencyPath;
}
