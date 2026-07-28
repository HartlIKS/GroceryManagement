package de.iks.grocery_manager.server.dto;

public record PageInfoDTO(
    int size,
    int number,
    long totalElements,
    int totalPages
) {
}
