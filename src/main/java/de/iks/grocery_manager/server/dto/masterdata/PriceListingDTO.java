package de.iks.grocery_manager.server.dto.masterdata;

import java.math.BigDecimal;
import java.util.UUID;

public record PriceListingDTO(
    UUID listPriceUUID,
    BigDecimal price
) {
}
