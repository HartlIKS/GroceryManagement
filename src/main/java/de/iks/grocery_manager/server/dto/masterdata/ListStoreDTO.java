package de.iks.grocery_manager.server.dto.masterdata;

import de.iks.grocery_manager.server.mapping.HasUUID_DTO;

import java.util.Currency;
import java.util.UUID;

public record ListStoreDTO(
    UUID uuid,
    String name,
    String logo,
    AddressDTO address,
    Currency currency
) implements HasUUID_DTO {
}
