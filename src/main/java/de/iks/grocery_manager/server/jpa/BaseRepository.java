package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.HasUUID;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.UUID;

public interface BaseRepository<E extends HasUUID> extends PanacheRepositoryBase<E, UUID> {
    default E saveAndFlush(E entity) {
        persistAndFlush(entity);
        return entity;
    }

    default boolean existsById(UUID id) {
        return findByIdOptional(id).isPresent();
    }
}
