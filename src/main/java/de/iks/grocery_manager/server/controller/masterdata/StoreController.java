package de.iks.grocery_manager.server.controller.masterdata;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.masterdata.StoreDTO;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.masterdata.Store;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/api/masterdata/store")
@Transactional
public class StoreController extends CRUDController<Store, StoreDTO, StoreRepository> {
    private final DTOMapper dtoMapper;
    public StoreController(StoreRepository repository, DTOMapper dtoMapper) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
        this.dtoMapper = dtoMapper;
    }

    @GET
    @JsonView(DTOViews.List.class)
    public PageDTO<StoreDTO> search(
        @QueryParam("name") @DefaultValue("") String name,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        return dtoMapper.map(
            repository
                .find("name LIKE '%' || ?1 || '%'", name.replace("%", "%%"))
                .page(page, size),
            dtoMapper::map
        );
    }
}
