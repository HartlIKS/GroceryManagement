package de.iks.grocery_manager.server.dto;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ProductGroupDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    @JsonView(DTOViews.Update.class)
    int version,
    String name,
    Map<UUID, BigDecimal> products
) implements HasUUID_DTO {
}
