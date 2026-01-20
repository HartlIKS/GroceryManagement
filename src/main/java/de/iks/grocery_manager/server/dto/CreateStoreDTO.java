package de.iks.grocery_manager.server.dto;

import java.util.Currency;

public record CreateStoreDTO(
    String name,
    String logo,
    AddressDTO address,
    Currency currency
) {
}
