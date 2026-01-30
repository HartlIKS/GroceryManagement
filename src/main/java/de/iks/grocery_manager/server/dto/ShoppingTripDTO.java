package de.iks.grocery_manager.server.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

public record ShoppingTripDTO(
    UUID uuid,
    UUID store,
    ZonedDateTime time,
    Map<UUID, BigDecimal> products
) {
}
