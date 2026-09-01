package de.iks.grocery_manager.server.dto.masterdata;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.util.UUID;

public record ProductDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    @JsonView(DTOViews.Update.class)
    int version,
    String name,
    String image,
    String EAN
) implements HasUUID_DTO {
}
