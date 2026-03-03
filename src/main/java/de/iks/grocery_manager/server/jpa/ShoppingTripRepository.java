package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.jpa.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingTripRepository extends JpaRepository<ShoppingTrip, UUID>, CrudRepositoryMapper.ShoppingTrips {
    Optional<? extends ShoppingTrip> findByUuidAndOwner(UUID uuid, String owner);
    void deleteByUuidAndOwner(UUID uuid, String owner);
    Page<ShoppingTrip> findByOwnerAndTimeBetween(String owner, Instant timeAfter, Instant timeBefore, Pageable pageable);
}
