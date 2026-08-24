package de.iks.grocery_manager.server.dto.masterdata;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.util.Currency;
import java.util.UUID;

public record StoreDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    String name,
    String logo,
    AddressDTO address,
    Currency currency
) implements HasUUID_DTO {
}
