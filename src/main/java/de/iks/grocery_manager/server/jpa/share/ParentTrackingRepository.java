package de.iks.grocery_manager.server.jpa.share;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.HasUUID;

import java.util.Optional;
import java.util.UUID;

public interface ParentTrackingRepository<E extends HasUUID> extends BaseRepository<E> {
    Optional<E> findByIdOptional(UUID parent, UUID uuid);
    void deleteById(UUID parent, UUID uuid);
}
