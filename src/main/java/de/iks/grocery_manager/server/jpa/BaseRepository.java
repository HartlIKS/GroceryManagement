package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.HasUUID;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public interface BaseRepository<E extends HasUUID> extends PanacheRepositoryBase<E, UUID> {
    default E saveAndFlush(E entity) {
        persistAndFlush(entity);
        return entity;
    }

    default boolean existsById(UUID id) {
        return findByIdOptional(id).isPresent();
    }

    default Stream<E> streamByIds(Collection<? extends UUID> ids) {
        return stream("uuid in ?1", ids);
    }
}
