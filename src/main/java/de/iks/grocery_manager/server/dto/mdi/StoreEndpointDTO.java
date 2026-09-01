package de.iks.grocery_manager.server.dto.mdi;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.mdi.ResponseType;

import java.util.UUID;

public record StoreEndpointDTO(
    @JsonView(DTOViews.List.class)
    UUID uuid,
    @JsonView(DTOViews.Update.class)
    int version,
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
