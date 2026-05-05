package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;
import de.iks.grocery_manager.server.model.mdi.ResponseType;

import java.util.UUID;

public record ProductEndpointDTO(
    UUID uuid,
    String name,
    String baseUrl,
    ParameterDTO pageSize,
    ParameterDTO page,
    ParameterDTO itemCount,
    ResponseType responseType,
    String basePath,
    String productIdPath,
    String productNamePath,
    String productImagePath,
    String productEANPath
) implements HasUUID_DTO {
}
