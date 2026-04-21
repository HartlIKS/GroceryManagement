package de.iks.grocery_manager.server.jpa.share;

import de.iks.grocery_manager.server.model.HasUUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface ParentTrackingRepository<E extends HasUUID> extends JpaRepository<E, UUID> {
    Optional<E> findById(UUID parent, UUID uuid);
    void deleteById(UUID parent, UUID uuid);
}
