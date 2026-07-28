package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.masterdata.CreateStoreDTO;
import de.iks.grocery_manager.server.dto.masterdata.ListStoreDTO;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.masterdata.Store;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/api/masterdata/store")
@Transactional
public class StoreController extends CRUDController.Standard<Store, ListStoreDTO, CreateStoreDTO, StoreRepository> {
    private final DTOMapper dtoMapper;
    public StoreController(StoreRepository repository, DTOMapper dtoMapper) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
        this.dtoMapper = dtoMapper;
    }

    @GET
    public PageDTO<ListStoreDTO> search(
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
