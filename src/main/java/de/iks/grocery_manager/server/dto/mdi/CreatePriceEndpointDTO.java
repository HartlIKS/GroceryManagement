package de.iks.grocery_manager.server.dto.mdi;

import de.iks.grocery_manager.server.dto.mdi.handling.ParameterDTO;
import de.iks.grocery_manager.server.dto.mdi.handling.ProductHandlingDTO;
import de.iks.grocery_manager.server.dto.mdi.handling.StoreHandlingDTO;
import de.iks.grocery_manager.server.model.mdi.ResponseType;

public record CreatePriceEndpointDTO(
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
) {
}
