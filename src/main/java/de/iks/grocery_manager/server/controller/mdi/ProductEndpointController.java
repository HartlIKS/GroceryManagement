package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.dto.mdi.CreateProductEndpointDTO;
import de.iks.grocery_manager.server.dto.mdi.ProductEndpointDTO;
import de.iks.grocery_manager.server.jpa.mdi.ProductEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.ProductEndpoint;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/api/masterdata/interface/{parentUuid}/endpoint/product",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ProductEndpointController extends EndpointController<ProductEndpoint, ProductEndpointDTO, CreateProductEndpointDTO, ProductEndpointRepository> {
    public ProductEndpointController(
        ProductEndpointRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper.Parented<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "masterdata", "interface", "{parentUuid}", "endpoint", "product", "{uuid}");
    }
}
