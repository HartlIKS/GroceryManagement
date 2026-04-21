package de.iks.grocery_manager.server.dto.mdi.handling;

public record PathDTO(
    String path
) implements ProductHandlingDTO, StoreHandlingDTO {
}
