package de.iks.grocery_manager.server.dto.masterdata;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdatePriceDTO(
    Instant validFrom,
    Instant validTo,
    BigDecimal price
) {
}
