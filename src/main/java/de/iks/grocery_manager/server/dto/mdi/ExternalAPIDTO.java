package de.iks.grocery_manager.server.dto.mdi;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.util.UUID;

public record ExternalAPIDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    @JsonView(DTOViews.Update.class)
    int version,
    String name
) implements HasUUID_DTO {
}
