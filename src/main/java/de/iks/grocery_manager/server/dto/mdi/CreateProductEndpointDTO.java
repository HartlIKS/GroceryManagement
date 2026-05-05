package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;
import de.iks.grocery_manager.server.model.mdi.ResponseType;

public record CreateProductEndpointDTO(
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
) {
}
