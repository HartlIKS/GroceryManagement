package de.iks.grocery_manager.server.dto.masterdata;

import de.iks.grocery_manager.server.dto.HasUUID_DTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ListPriceDTO(
    UUID uuid,
    UUID store,
    UUID product,
    Instant validFrom,
    Instant validTo,
    BigDecimal price
) implements HasUUID_DTO {
}
