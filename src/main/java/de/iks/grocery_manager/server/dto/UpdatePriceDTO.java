package de.iks.grocery_manager.server.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record UpdatePriceDTO(
    ZonedDateTime validFrom,
    ZonedDateTime validTo,
    BigDecimal price
) {
}
