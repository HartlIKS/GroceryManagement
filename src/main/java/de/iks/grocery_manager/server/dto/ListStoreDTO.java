package de.iks.grocery_manager.server.dto;

import java.util.Currency;
import java.util.UUID;

public record ListStoreDTO(
    UUID uuid,
    String name,
    String logo,
    AddressDTO address,
    Currency currency
) {
}
