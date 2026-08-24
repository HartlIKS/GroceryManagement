package de.iks.grocery_manager.server.dto;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ShoppingListDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    String name,
    boolean repeating,
    Map<UUID, BigDecimal> products,
    Map<UUID, BigDecimal> productGroups
) implements HasUUID_DTO {
}
