package de.iks.grocery_manager.server.dto.masterdata;

public record AddressDTO(
    String country,
    String city,
    String zip,
    String street,
    String number
) {
}
