package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.dto.mdi.CreateProductEndpointDTO;
import de.iks.grocery_manager.server.dto.mdi.ProductEndpointDTO;
import de.iks.grocery_manager.server.jpa.mdi.ProductEndpointRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.mdi.ProductEndpoint;
import jakarta.ws.rs.Path;
import jakarta.transaction.Transactional;

@Path("/api/masterdata/interface/{parentUuid}/endpoint/product")
@Transactional
public class ProductEndpointController extends EndpointController<ProductEndpoint, ProductEndpointDTO, CreateProductEndpointDTO, ProductEndpointRepository> {
    public ProductEndpointController(
        ProductEndpointRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper.Parented<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
    }
}
