package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.util.Map;
import java.util.UUID;

public record ExternalAPIDTO(
    UUID uuid,
    String name,
    Map<UUID, String> productMappings,
    Map<UUID, String> storeMappings
) implements HasUUID_DTO {
}
