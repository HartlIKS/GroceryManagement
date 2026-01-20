package de.iks.grocery_manager.server.dto;

public record CreateProductDTO(
    String name,
    String image,
    String EAN
) {
}
