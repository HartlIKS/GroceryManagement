package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;
import de.iks.grocery_manager.server.dto.mdi.handling.ProductHandlingDTO;
import de.iks.grocery_manager.server.dto.mdi.handling.StoreHandlingDTO;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.mdi.ResponseType;

import java.util.UUID;

public record PriceEndpointDTO(
    UUID uuid,
    String name,
    String baseUrl,
    ParameterDTO pageSize,
    ParameterDTO page,
    ParameterDTO itemCount,
    ResponseType responseType,
    String basePath,
    ProductHandlingDTO productHandling,
    StoreHandlingDTO storeHandling,
    String pricePath,
    String timeFormat,
    String validFromPath,
    String validUntilPath
) implements HasUUID_DTO {
}
