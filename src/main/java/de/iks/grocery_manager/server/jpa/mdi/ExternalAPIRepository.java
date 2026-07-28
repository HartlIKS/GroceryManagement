package de.iks.grocery_manager.server.jpa.mdi;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ExternalAPIRepository implements BaseRepository<ExternalAPI> {
    @RegisterForReflection
    private record Key(
        @ProjectedFieldName("key(m).uuid") UUID uuid
    ) {}
    @RegisterForReflection
    private record Value(
        @ProjectedFieldName("value(m)") String value
    ) {}

    public Optional<UUID> translateInboundProducts(UUID table, String remoteId) {
        return find(
            "FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and value(m) = :remoteId",
            Map.of("table", table, "remoteId", remoteId)
        ).project(Key.class).firstResultOptional().map(Key::uuid);
    }
    public Optional<String> translateOutboundProducts(UUID table, UUID localId) {
        return find(
            "FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and key(m).uuid = :localId",
            Map.of("table", table, "localId", localId)
        ).project(Value.class).firstResultOptional().map(Value::value);
    }

    public Optional<UUID> translateInboundStores(UUID table, String remoteId) {
        return find(
            "FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and value(m) = :remoteId",
            Map.of("table", table, "remoteId", remoteId)
        ).project(Key.class).firstResultOptional().map(Key::uuid);
    }
    public Optional<String> translateOutboundStores(UUID table, UUID localId) {
        return find(
            "FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and key(m).uuid = :localId",
            Map.of("table", table, "localId", localId)
        ).project(Value.class).firstResultOptional().map(Value::value);
    }
}
