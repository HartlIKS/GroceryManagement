package de.iks.grocery_manager.server.jpa.mdi;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    @RegisterForReflection
    private record KeyValue(
        @ProjectedFieldName("key(m).uuid") UUID uuid,
        @ProjectedFieldName("value(m)") String value
    ) {}

    public Map<String, UUID> massTranslateInboundProducts(UUID table, Collection<? extends String> remoteIds) {
        return find(
            "FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and value(m) in :remoteIds",
            Map.of("table", table, "remoteIds", remoteIds)
        ).project(KeyValue.class).stream().collect(
            Collectors.toUnmodifiableMap(
                KeyValue::value,
                KeyValue::uuid
            )
        );
    }
    public Optional<UUID> translateInboundProducts(UUID table, String remoteId) {
        return find(
            "FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and value(m) = :remoteId",
            Map.of("table", table, "remoteId", remoteId)
        ).project(Key.class).firstResultOptional().map(Key::uuid);
    }

    public Map<UUID, String> massTranslateOutboundProducts(UUID table, Collection<? extends UUID> localIds) {
        return find(
            "FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and key(m).uuid in :localIds",
            Map.of("table", table, "localIds", localIds)
        ).project(KeyValue.class).stream().collect(
            Collectors.toUnmodifiableMap(
                KeyValue::uuid,
                KeyValue::value
            )
        );
    }
    public Optional<String> translateOutboundProducts(UUID table, UUID localId) {
        return find(
            "FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and key(m).uuid = :localId",
            Map.of("table", table, "localId", localId)
        ).project(Value.class).firstResultOptional().map(Value::value);
    }

    public Map<String, UUID> massTranslateInboundStores(UUID table, Collection<? extends String> remoteIds) {
        return find(
            "FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and value(m) in :remoteIds",
            Map.of("table", table, "remoteIds", remoteIds)
        ).project(KeyValue.class).stream().collect(
            Collectors.toUnmodifiableMap(
                KeyValue::value,
                KeyValue::uuid
            )
        );
    }
    public Optional<UUID> translateInboundStores(UUID table, String remoteId) {
        return find(
            "FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and value(m) = :remoteId",
            Map.of("table", table, "remoteId", remoteId)
        ).project(Key.class).firstResultOptional().map(Key::uuid);
    }

    public Map<UUID, String> massTranslateOutboundStores(UUID table, Collection<? extends UUID> localIds) {
        return find(
            "FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and key(m).uuid in :localIds",
            Map.of("table", table, "localIds", localIds)
        ).project(KeyValue.class).stream().collect(
            Collectors.toUnmodifiableMap(
                KeyValue::uuid,
                KeyValue::value
            )
        );
    }
    public Optional<String> translateOutboundStores(UUID table, UUID localId) {
        return find(
            "FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and key(m).uuid = :localId",
            Map.of("table", table, "localId", localId)
        ).project(Value.class).firstResultOptional().map(Value::value);
    }
}
