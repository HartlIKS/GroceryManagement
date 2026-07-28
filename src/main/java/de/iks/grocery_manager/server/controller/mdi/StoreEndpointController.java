package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.dto.mdi.CreateStoreEndpointDTO;
import de.iks.grocery_manager.server.dto.mdi.StoreEndpointDTO;
import de.iks.grocery_manager.server.jpa.mdi.StoreEndpointRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.mdi.StoreEndpoint;
import jakarta.ws.rs.Path;
import jakarta.transaction.Transactional;

@Path("/api/masterdata/interface/{parentUuid}/endpoint/store")
@Transactional
public class StoreEndpointController extends EndpointController<StoreEndpoint, StoreEndpointDTO, CreateStoreEndpointDTO, StoreEndpointRepository> {
    public StoreEndpointController(
        StoreEndpointRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper.Parented<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
    }
}
