package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.dto.mdi.CreatePriceEndpointDTO;
import de.iks.grocery_manager.server.dto.mdi.PriceEndpointDTO;
import de.iks.grocery_manager.server.jpa.mdi.PriceEndpointRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.mdi.PriceEndpoint;
import jakarta.ws.rs.Path;
import jakarta.transaction.Transactional;

@Path("/api/masterdata/interface/{parentUuid}/endpoint/price")
@Transactional
public class PriceEndpointController extends EndpointController<PriceEndpoint, PriceEndpointDTO, CreatePriceEndpointDTO, PriceEndpointRepository> {
    public PriceEndpointController(
        PriceEndpointRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper.Parented<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
    }
}
