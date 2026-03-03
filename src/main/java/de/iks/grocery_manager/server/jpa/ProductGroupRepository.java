package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.jpa.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.ProductGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductGroupRepository extends JpaRepository<ProductGroup, UUID>, CrudRepositoryMapper.ProductGroups {
    Optional<? extends ProductGroup> findByUuidAndOwner(UUID uuid, String owner);
    void deleteByUuidAndOwner(UUID uuid, String owner);
    Page<? extends ProductGroup> findAllByOwnerAndNameContainingIgnoreCase(String owner, String name, Pageable pageable);
}
