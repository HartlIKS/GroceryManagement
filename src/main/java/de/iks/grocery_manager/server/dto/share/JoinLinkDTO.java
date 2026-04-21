package de.iks.grocery_manager.server.dto.share;

import de.iks.grocery_manager.server.dto.HasUUID_DTO;
import de.iks.grocery_manager.server.model.share.Permissions;

import java.time.Instant;
import java.util.UUID;

public record JoinLinkDTO(
    UUID uuid,
    String name,
    Permissions permissions,
    boolean active,
    boolean singleUse,
    Instant validTo,
    int numUsers
) implements HasUUID_DTO {
}
