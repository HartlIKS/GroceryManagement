package de.iks.grocery_manager.server.dto.masterdata;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record ListPriceDTO(
    UUID uuid,
    UUID store,
    UUID product,
    ZonedDateTime validFrom,
    ZonedDateTime validTo,
    BigDecimal price
) {
}
