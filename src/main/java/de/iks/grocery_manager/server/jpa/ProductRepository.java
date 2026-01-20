package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
