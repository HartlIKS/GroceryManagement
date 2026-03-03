package de.iks.grocery_manager.server.dto.share;

import de.iks.grocery_manager.server.model.share.Permissions;

import java.time.Instant;

public record CreateJoinLinkDTO(
    String name,
    Permissions permissions,
    Boolean active,
    Boolean singleUse,
    Instant validTo
) {
}