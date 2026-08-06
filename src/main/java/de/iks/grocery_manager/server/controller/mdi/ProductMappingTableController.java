package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.MappingHandler;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import jakarta.ws.rs.Path;
import jakarta.transaction.Transactional;

@Path("/api/masterdata/interface/{uuid}/mapping/product")
@Transactional
public class ProductMappingTableController extends MappingTableController<ExternalAPI, ExternalAPIRepository, Product, ProductRepository> {
    public ProductMappingTableController(
        ExternalAPIRepository repository,
        ProductRepository mappedRepository,
        DTOMapper dtoMapper
    ) {
        super(repository, new MappingHandler<>(
            ExternalAPI::getProductMappings,
            repository::translateInboundProducts,
            repository::massTranslateInboundProducts,
            repository::translateOutboundProducts,
            repository::massTranslateOutboundProducts
        ), mappedRepository, dtoMapper);
    }
}
