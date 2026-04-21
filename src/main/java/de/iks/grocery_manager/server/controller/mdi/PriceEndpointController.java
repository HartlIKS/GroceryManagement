package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.dto.mdi.CreatePriceEndpointDTO;
import de.iks.grocery_manager.server.dto.mdi.PriceEndpointDTO;
import de.iks.grocery_manager.server.jpa.mdi.PriceEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.PriceEndpoint;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/api/masterdata/interface/{parentUuid}/endpoint/price",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class PriceEndpointController extends EndpointController<PriceEndpoint, PriceEndpointDTO, CreatePriceEndpointDTO, PriceEndpointRepository> {
    public PriceEndpointController(
        PriceEndpointRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper.Parented<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "masterdata", "interface", "{parentUuid}", "endpoint", "price", "{uuid}");
    }
}
