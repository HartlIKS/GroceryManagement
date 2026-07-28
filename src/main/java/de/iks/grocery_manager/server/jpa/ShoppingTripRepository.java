package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.ShoppingTrip;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Map;

@ApplicationScoped
public class ShoppingTripRepository implements OwnerTrackingJpaRepository<ShoppingTrip> {
    public PanacheQuery<ShoppingTrip> findByOwnerAndTimeBetween(String owner, Instant from, Instant to) {
        return find(
            "owner = :owner AND time BETWEEN :from AND :to",
            Map.of("owner", owner, "from", from, "to", to)
        );
    }
}
