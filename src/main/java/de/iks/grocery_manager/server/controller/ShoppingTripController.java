package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateShoppingTripDTO;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.ShoppingTripDTO;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper.Owned;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Product;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import org.jboss.resteasy.reactive.RestResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

@Path("/api/shoppingTrips")
@Transactional
public class ShoppingTripController
    extends OwnerTrackingCRUDController.Standard<ShoppingTrip, ShoppingTripDTO, CreateShoppingTripDTO,
    ShoppingTripRepository> {
    private final DTOMapper dtoMapper;

    public ShoppingTripController(
        ShoppingTripRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new Owned<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
        this.dtoMapper = dtoMapper;
    }

    @POST
    @Path("{uuid}/add")
    public RestResponse<ShoppingTripDTO> addToShoppingTrip(
        @PathParam("uuid") UUID uuid,
        Map<UUID, BigDecimal> products
    ) {
        return repository
            .findByUuidAndOwner(uuid, userInfo.getOwner())
            .map(p -> {
                Map<Product, BigDecimal> prods = p.getProducts();
                Map<Product, BigDecimal> newProds = dtoMapper.toProducts(products);
                newProds.forEach((product, amount) -> prods.merge(
                    product,
                    amount,
                    BigDecimal::add
                ));
                return p;
            })
            .map(repository::saveAndFlush)
            .map(dtoMapper::map)
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @GET
    public RestResponse<PageDTO<ShoppingTripDTO>> search(
        @QueryParam("from") ZonedDateTime from,
        @QueryParam("to") ZonedDateTime to,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        if(from == null) from = ZonedDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT, ZoneId.systemDefault());
        if(to == null) to = from.plusWeeks(1);
        return RestResponse.ok(
            dtoMapper.map(
                repository
                    .findByOwnerAndTimeBetween(userInfo.getOwner(), from.toInstant(), to.toInstant())
                    .page(page, size),
                dtoMapper::map
            )
        );
    }
}
