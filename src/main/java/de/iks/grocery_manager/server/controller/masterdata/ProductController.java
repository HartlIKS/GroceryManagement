package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.masterdata.CreateProductDTO;
import de.iks.grocery_manager.server.dto.masterdata.ListProductDTO;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.masterdata.Product;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/api/masterdata/product")
@Transactional
public class ProductController extends CRUDController.Standard<Product, ListProductDTO, CreateProductDTO, ProductRepository> {
    private final DTOMapper dtoMapper;
    public ProductController(
        ProductRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
        this.dtoMapper = dtoMapper;
    }

    @GET
    public PageDTO<ListProductDTO> search(
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
