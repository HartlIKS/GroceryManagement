package de.iks.grocery_manager.server.dto.mdi;

public record AddressPathsDTO(
    String countryPath,
    String cityPath,
    String zipPath,
    String streetPath,
    String numberPath
) {
}
