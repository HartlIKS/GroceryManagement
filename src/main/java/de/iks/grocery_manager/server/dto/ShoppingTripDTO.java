package de.iks.grocery_manager.server.dto;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ShoppingTripDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    UUID store,
    Instant time,
    Map<UUID, BigDecimal> products
) implements HasUUID_DTO {
}
