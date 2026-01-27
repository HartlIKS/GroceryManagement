package de.iks.grocery_manager.server.dto;

import java.util.UUID;

public record ListProductDTO(
    UUID uuid,
    String name,
    String image,
    String EAN
) {
}
