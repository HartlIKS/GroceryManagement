package de.iks.grocery_manager.server.dto;

import java.util.List;
import java.util.UUID;

public record ListProductGroupDTO(
    UUID uuid,
    String name,
    List<UUID> products
) {
}
