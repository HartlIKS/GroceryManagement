package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.ShoppingList;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class ShoppingListRepository implements OwnerTrackingJpaRepository<ShoppingList> {
    public void deleteByUuidAndOwnerAndNotRepeating(UUID uuid, String owner) {
        delete(
            "uuid = :uuid and owner = :owner and not repeating",
            Map.of("uuid", uuid, "owner", owner)
        );
    }
    public PanacheQuery<ShoppingList> findAllByOwnerAndNameContainingIgnoreCase(String owner, String name) {
        return find(
            "owner = :owner and name like '%' || :name || '%'",
            Map.of("owner", owner, "name", name)
        );
    }
}
