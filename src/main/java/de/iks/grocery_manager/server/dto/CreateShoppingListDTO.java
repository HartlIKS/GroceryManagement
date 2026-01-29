package de.iks.grocery_manager.server.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record CreateShoppingListDTO(
    String name,
    Map<UUID, BigDecimal> products,
    Map<UUID, BigDecimal> productGroups
) {
}
