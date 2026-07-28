package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.ProductGroup;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;

@ApplicationScoped
public class ProductGroupRepository implements OwnerTrackingJpaRepository<ProductGroup> {
    public PanacheQuery<ProductGroup> findAllByOwnerAndNameContainingIgnoreCase(String owner, String name) {
        return find(
            "owner = :owner and lower(name) like lower(:name)",
            Map.of("owner", owner, "name", String.format("%%%s%%", name))
        );
    }
}
