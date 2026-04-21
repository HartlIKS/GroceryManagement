package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;

public record CreateProductEndpointDTO(
    String name,
    String baseUrl,
    ParameterDTO pageSize,
    ParameterDTO page,
    ParameterDTO itemCount,
    String basePath,
    String productIdPath,
    String productNamePath,
    String productImagePath,
    String productEANPath
) {
}
