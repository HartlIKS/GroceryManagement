package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;
import de.iks.grocery_manager.server.model.mdi.ResponseType;

public record CreateStoreEndpointDTO(
    String name,
    String baseUrl,
    ParameterDTO pageSize,
    ParameterDTO page,
    ParameterDTO itemCount,
    ResponseType responseType,
    String basePath,
    String storeIdPath,
    String storeNamePath,
    String storeLogoPath,
    String addressPath,
    AddressPathsDTO addressPaths,
    String storeCurrencyPath
) {
}
