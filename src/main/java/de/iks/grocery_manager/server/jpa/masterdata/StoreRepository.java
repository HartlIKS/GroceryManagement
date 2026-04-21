package de.iks.grocery_manager.server.jpa.masterdata;

import de.iks.grocery_manager.server.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.masterdata.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID>, CrudRepositoryMapper.Stores {
    Page<Store> findAllByNameContainingIgnoreCase(String name, Pageable pageable);
}
