package de.iks.grocery_manager.server.dto.mdi;

import java.util.Map;
import java.util.UUID;

public record CreateExternalAPIDTO(
    String name,
    Map<UUID, String> productMappings,
    Map<UUID, String> storeMappings
) {
}
