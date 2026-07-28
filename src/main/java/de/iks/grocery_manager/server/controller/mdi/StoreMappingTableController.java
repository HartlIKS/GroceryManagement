package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.MappingHandler;
import de.iks.grocery_manager.server.model.masterdata.Store;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Path;
import jakarta.transaction.Transactional;

@Path("/api/masterdata/interface/{uuid}/mapping/store")
@Transactional
public class StoreMappingTableController extends MappingTableController<ExternalAPI, ExternalAPIRepository, Store, StoreRepository> {
    public StoreMappingTableController(
        ExternalAPIRepository repository,
        StoreRepository mappedRepository,
        DTOMapper dtoMapper
    ) {
        super(repository, new MappingHandler<>(ExternalAPI::getStoreMappings, repository::translateInboundStores, repository::translateOutboundStores), mappedRepository, dtoMapper);
    }
}
