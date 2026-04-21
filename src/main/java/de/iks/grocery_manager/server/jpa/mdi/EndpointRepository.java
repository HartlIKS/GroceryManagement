package de.iks.grocery_manager.server.jpa.mdi;

import de.iks.grocery_manager.server.jpa.share.ParentTrackingRepository;
import de.iks.grocery_manager.server.model.mdi.Endpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface EndpointRepository<E extends Endpoint> extends ParentTrackingRepository<E> {
    @Override
    @Query("SELECT e FROM Endpoint e WHERE e.uuid = :uuid AND e.api.uuid = :parent")
    Optional<E> findById(UUID parent, UUID uuid);

    @Override
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Endpoint e WHERE e.uuid = :uuid AND e.api.uuid = :parent")
    void deleteById(UUID parent, UUID uuid);

    Page<E> findAllByApi_UuidAndNameContaining(UUID uuid, String name, Pageable pageable);
}
