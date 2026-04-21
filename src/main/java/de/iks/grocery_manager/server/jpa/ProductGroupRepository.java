package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.jpa.mapping.CrudRepositoryMapper.ProductGroups;
import de.iks.grocery_manager.server.model.ProductGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductGroupRepository extends OwnerTrackingJpaRepository<ProductGroup>, ProductGroups {
    Page<? extends ProductGroup> findAllByOwnerAndNameContainingIgnoreCase(String owner, String name, Pageable pageable);
}
