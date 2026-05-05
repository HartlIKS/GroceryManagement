package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.mdi.ResponseType;

import java.util.UUID;

public record StoreEndpointDTO(
    UUID uuid,
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
) implements HasUUID_DTO {
}
