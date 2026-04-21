package de.iks.grocery_manager.server.model.mdi;

import de.iks.grocery_manager.server.model.mdi.handling.*;
import de.iks.grocery_manager.server.model.mdi.handling.Parameter;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table
public class PriceEndpoint extends Endpoint {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductHandlingType productHandlingType;
    @AttributeOverride(name = "header", column = @Column(name = "product_header"))
    @AttributeOverride(name = "queryParameter", column = @Column(name = "product_query_parameter"))
    private Parameter productParameters;
    @AttributeOverride(name = "path", column = @Column(name = "product_path"))
    private Path productPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreHandlingType storeHandlingType;
    @AttributeOverride(name = "header", column = @Column(name = "store_header"))
    @AttributeOverride(name = "queryParameter", column = @Column(name = "store_query_parameter"))
    private Parameter storeParameters;
    @AttributeOverride(name = "path", column = @Column(name = "store_path"))
    private Path storePath;

    @Column(nullable = false)
    private String pricePath;

    @Column(nullable = false)
    private String timeFormat;

    @Column(nullable = false)
    private String validFromPath;

    @Column(nullable = false)
    private String validUntilPath;

    public ProductHandling getProductHandling() {
        return switch(productHandlingType) {
            case PARAMETER -> productParameters;
            case PATH -> productPath;
        };
    }

    @Transient
    public void setProductHandling(ProductHandling handling) {
        if(handling instanceof Parameter p) {
            productPath = null;
            productParameters = p;
            productHandlingType = ProductHandlingType.PARAMETER;
        } else if(handling instanceof Path p) {
            productPath = p;
            productParameters = null;
            productHandlingType = ProductHandlingType.PATH;
        }
    }

    public StoreHandling getStoreHandling() {
        return switch(storeHandlingType) {
            case PARAMETER -> storeParameters;
            case PATH -> storePath;
            case ONE_FOR_ALL -> new OneForAll();
        };
    }

    @Transient
    public void setStoreHandling(StoreHandling handling) {
        if(handling instanceof Parameter p) {
            storePath = null;
            storeParameters = p;
            storeHandlingType = StoreHandlingType.PARAMETER;
        } else if(handling instanceof Path p) {
            storePath = p;
            storeParameters = null;
            storeHandlingType = StoreHandlingType.PATH;
        } else if(handling instanceof OneForAll) {
            storePath = null;
            storeParameters = null;
            storeHandlingType = StoreHandlingType.ONE_FOR_ALL;
        } else {
            throw new IllegalArgumentException("Invalid store handling type: " + handling);
        }
    }
}
