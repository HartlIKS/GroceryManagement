package de.iks.grocery_manager.server.dto.share;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.share.Permissions;

import java.time.Instant;
import java.util.UUID;

public record JoinLinkDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    String name,
    Permissions permissions,
    boolean active,
    boolean singleUse,
    Instant validTo,
    @JsonView(DTOViews.List.class)
    int numUsers
) implements HasUUID_DTO {
}
