package de.iks.grocery_manager.server.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record ShoppingListDTO(
    UUID uuid,
    String name,
    boolean repeating,
    Map<UUID, BigDecimal> products,
    Map<UUID, BigDecimal> productGroups
) implements HasUUID_DTO {
}
