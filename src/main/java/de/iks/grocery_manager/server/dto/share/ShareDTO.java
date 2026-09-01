package de.iks.grocery_manager.server.dto.share;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.share.Permissions;

import java.util.UUID;

public record ShareDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    @JsonView(DTOViews.Update.class)
    int version,
    String name,
    @JsonView(DTOViews.List.class)
    Permissions permissions
) implements HasUUID_DTO {
}
