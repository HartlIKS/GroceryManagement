package de.iks.grocery_manager.server.jpa.mdi;

import de.iks.grocery_manager.server.jpa.share.ParentTrackingRepository;
import de.iks.grocery_manager.server.model.mdi.Endpoint;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface EndpointRepository<E extends Endpoint> extends ParentTrackingRepository<E> {
    @Override
    default Optional<E> findByIdOptional(UUID parent, UUID uuid) {
        return find(
            "uuid = :uuid AND api.uuid = :parent",
            Map.of("uuid", uuid, "parent", parent)
        ).firstResultOptional();
    }

    @Override
    default void deleteById(UUID parent, UUID uuid) {
        delete(
            "uuid = :uuid AND api.uuid = :parent",
            Map.of("uuid", uuid, "parent", parent)
        );
    }
}
