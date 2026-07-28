package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.HasOwner;
import de.iks.grocery_manager.server.model.HasUUID;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface OwnerTrackingJpaRepository<E extends @NonNull HasUUID & HasOwner> extends BaseRepository<E> {
    default Optional<? extends E> findByUuidAndOwner(UUID uuid, String owner) {
        return find(
            "uuid = :uuid and owner = :owner",
            Map.of("uuid", uuid, "owner", owner)
        ).firstResultOptional();
    }

    default void deleteByUuidAndOwner(UUID uuid, String owner) {
        delete(
            "uuid = :uuid and owner = :owner",
            Map.of("uuid", uuid, "owner", owner)
        );
    }

    default void deleteAllByOwner(String owner) {
        delete("owner", owner);
    }
}
