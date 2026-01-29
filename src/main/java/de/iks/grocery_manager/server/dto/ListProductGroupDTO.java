package de.iks.grocery_manager.server.dto;

import java.util.Set;
import java.util.UUID;

public record ListProductGroupDTO(
    UUID uuid,
    String name,
    Set<UUID> products
) {
}
