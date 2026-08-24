package de.iks.grocery_manager.server.dto.masterdata;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PriceDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    @JsonView(DTOViews.Create.class)
    UUID store,
    @JsonView(DTOViews.Create.class)
    UUID product,
    Instant validFrom,
    Instant validTo,
    BigDecimal price
) implements HasUUID_DTO {
}
