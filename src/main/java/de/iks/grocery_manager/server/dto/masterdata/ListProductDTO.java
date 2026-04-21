package de.iks.grocery_manager.server.dto.masterdata;

import de.iks.grocery_manager.server.dto.HasUUID_DTO;

import java.util.UUID;

public record ListProductDTO(
    UUID uuid,
    String name,
    String image,
    String EAN
) implements HasUUID_DTO {
}
