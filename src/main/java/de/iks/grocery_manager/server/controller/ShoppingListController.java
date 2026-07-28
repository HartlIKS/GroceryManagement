package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateShoppingListDTO;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.ShoppingListDTO;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper.Owned;
import de.iks.grocery_manager.server.model.ShoppingList;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;
import java.util.UUID;

@Path("/api/shoppingLists")
@Transactional
public class ShoppingListController
    extends OwnerTrackingCRUDController.Standard<ShoppingList, ShoppingListDTO, CreateShoppingListDTO,
    ShoppingListRepository> {
    private final DTOMapper dtoMapper;
    private final UriInfo uriInfo;

    public ShoppingListController(
        ShoppingListRepository repository,
        DTOMapper dtoMapper,
        UriInfo uriInfo
    ) {
        super(repository, new Owned<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
        this.dtoMapper = dtoMapper;
        this.uriInfo = uriInfo;
    }

    @Override
    public void delete(
        UUID uuid
    ) {
        if(uriInfo.getQueryParameters().getOrDefault("ifNonRepeating", List.of()).stream().anyMatch(Boolean::parseBoolean)) {
            repository.deleteByUuidAndOwnerAndNotRepeating(uuid, userInfo.getOwner());
        } else {
            repository.deleteByUuidAndOwner(uuid, userInfo.getOwner());
        }
    }

    @GET
    public RestResponse<PageDTO<ShoppingListDTO>> search(
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
