package de.iks.grocery_manager.server.jpa.masterdata;

import de.iks.grocery_manager.server.jpa.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.masterdata.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID>, CrudRepositoryMapper.Products {
    Page<Product> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
