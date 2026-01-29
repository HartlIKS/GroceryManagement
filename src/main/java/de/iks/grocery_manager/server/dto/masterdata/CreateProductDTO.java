package de.iks.grocery_manager.server.dto.masterdata;

public record CreateProductDTO(
    String name,
    String image,
    String EAN
) {
}
