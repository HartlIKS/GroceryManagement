package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;

public record CreateStoreEndpointDTO(
    String name,
    String baseUrl,
    ParameterDTO pageSize,
    ParameterDTO page,
    ParameterDTO itemCount,
    String basePath,
    String storeIdPath,
    String storeNamePath,
    String storeLogoPath,
    String addressPath,
    AddressPathsDTO addressPaths,
    String storeCurrencyPath
) {
}
