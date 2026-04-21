package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface ShoppingTripRepository extends OwnerTrackingJpaRepository<ShoppingTrip>, CrudRepositoryMapper.ShoppingTrips {
    Page<ShoppingTrip> findByOwnerAndTimeBetween(String owner, Instant timeAfter, Instant timeBefore, Pageable pageable);
}
