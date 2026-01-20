package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    Page<Store> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
