package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.stream.Stream;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, UUID> {
    Stream<? extends ProductGroup> findAllByOwner(String owner);
}
