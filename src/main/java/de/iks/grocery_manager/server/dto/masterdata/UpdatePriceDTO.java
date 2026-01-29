package de.iks.grocery_manager.server.dto.masterdata;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record UpdatePriceDTO(
    ZonedDateTime validFrom,
    ZonedDateTime validTo,
    BigDecimal price
) {
}
