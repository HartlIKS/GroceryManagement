package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.jpa.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.ShoppingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShoppingListRepository extends OwnerTrackingJpaRepository<ShoppingList>, CrudRepositoryMapper.ShoppingLists {
    void deleteByUuidAndOwnerAndRepeatingIsFalse(UUID uuid, String owner);
    Page<? extends ShoppingList> findAllByOwnerAndNameContainingIgnoreCase(String owner, String name, Pageable pageable);
}
