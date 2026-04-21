package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.HasOwner;
import de.iks.grocery_manager.server.model.HasUUID;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface OwnerTrackingJpaRepository<E extends HasUUID & HasOwner> extends JpaRepository<@NonNull E, UUID> {
    Optional<? extends @NonNull E> findByUuidAndOwner(UUID uuid, String owner);
    void deleteByUuidAndOwner(UUID uuid, String owner);

    void deleteAllByOwner(String owner);
}
