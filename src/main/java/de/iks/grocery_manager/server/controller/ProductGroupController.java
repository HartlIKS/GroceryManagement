package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateProductGroupDTO;
import de.iks.grocery_manager.server.dto.ListProductGroupDTO;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper.Owned;
import de.iks.grocery_manager.server.model.ProductGroup;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.jboss.resteasy.reactive.RestResponse;

@Path("/api/productGroups")
@Transactional
public class ProductGroupController
    extends OwnerTrackingCRUDController.Standard<ProductGroup, ListProductGroupDTO, CreateProductGroupDTO,
    ProductGroupRepository> {
    private final DTOMapper dtoMapper;

    public ProductGroupController(
        ProductGroupRepository repository,
        DTOMapper dtoMapper
    ) {
        super(
            repository,
            new Owned<>(dtoMapper::map, dtoMapper::create, dtoMapper::update)
        );
        this.dtoMapper = dtoMapper;
    }

    @GET
    public RestResponse<PageDTO<ListProductGroupDTO>> search(
        @QueryParam("name") @DefaultValue("") String name,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        return RestResponse.ok(
            dtoMapper.map(
                repository
                    .findAllByOwnerAndNameContainingIgnoreCase(userInfo.getOwner(), name)
                    .page(page, size),
                dtoMapper::map
            )
        );
    }
}
