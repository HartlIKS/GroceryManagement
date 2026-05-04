package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.util.UUID;

public record ExternalAPIDTO(
    UUID uuid,
    String name
) implements HasUUID_DTO {
}
