package de.iks.grocery_manager.server.dto.masterdata;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

public record CreatePriceListingDTO(
    UUID store,
    UUID product,
    ZonedDateTime validFrom,
    ZonedDateTime validTo,
    BigDecimal price
) {
}
