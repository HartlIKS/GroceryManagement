package de.iks.grocery_manager.server.dto;

public record AddressDTO(
    String country,
    String city,
    String zip,
    String street,
    String number
) {
}
