package de.iks.grocery_manager.server.dto;

import java.util.Currency;
import java.util.UUID;

public record ListStoreDTO(
    UUID uuid,
    String name,
    AddressDTO address,
    Currency currency
) {
}
