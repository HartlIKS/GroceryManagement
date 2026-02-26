package de.iks.grocery_manager.server.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CreateShoppingTripDTO(
    UUID store,
    Instant time,
    Map<UUID, BigDecimal> products
) {
}
