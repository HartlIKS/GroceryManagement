package de.iks.grocery_manager.server.dto.share;

import de.iks.grocery_manager.server.model.share.Permissions;

import java.util.UUID;

public record ShareDTO(
    UUID uuid,
    String name,
    Permissions permissions
) {
}
