package de.iks.grocery_manager.server.mapping;

import de.iks.grocery_manager.server.model.HasUUID;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public record MappingHandler<E extends HasUUID, E2 extends HasUUID>(
    Function<E, Map<E2, String>> getMappings,
    BiFunction<UUID, String, Optional<UUID>> translateInbound,
    BiFunction<UUID, UUID, Optional<String>> translateOutbound
) {
}
