package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.mdi.CreateExternalAPIDTO;
import de.iks.grocery_manager.server.dto.mdi.ExternalAPIDTO;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.QueryParam;

@Path("/api/masterdata/interface")
@Transactional
public class ExternalAPIController extends CRUDController.Standard<ExternalAPI, ExternalAPIDTO, CreateExternalAPIDTO, ExternalAPIRepository> {
    private final DTOMapper dtoMapper;
    public ExternalAPIController(
        ExternalAPIRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
        this.dtoMapper = dtoMapper;
    }

    @GET
    public PageDTO<ExternalAPIDTO> search(
        @QueryParam("name") @DefaultValue("") String name,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        return dtoMapper.map(
            repository
                .find("name LIKE '%' || ?1 || '%'", name)
                .page(page, size),
            dtoMapper::map
        );
    }
}
