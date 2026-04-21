package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateShoppingTripDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.EntityMapper.Owned;
import de.iks.grocery_manager.server.dto.ShoppingTripDTO;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static de.iks.grocery_manager.server.util.OwnerUtils.getOwner;

@RestController
@RequestMapping(
    path = "/api/shoppingTrips",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ShoppingTripController extends OwnerTrackingCRUDController.Standard<ShoppingTrip, ShoppingTripDTO, CreateShoppingTripDTO, ShoppingTripRepository> {
    private final DTOMapper dtoMapper;
    public ShoppingTripController(
        ShoppingTripRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new Owned<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), new String[] {"api", "shoppingTrips", "{uuid}"});
        this.dtoMapper = dtoMapper;
    }

    @PostMapping("/{uuid}/add")
    public ResponseEntity<ShoppingTripDTO> addToShoppingTrip(
        @PathVariable UUID uuid,
        @RequestBody Map<UUID, BigDecimal> products,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            repository
                .findByUuidAndOwner(uuid, getOwner(principal))
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
        );
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ShoppingTripDTO>> search(
        @RequestParam(required = false) ZonedDateTime from,
        @RequestParam(required = false) ZonedDateTime to,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal Object principal
    ) {
        if(from == null) from = ZonedDateTime.now();
        if(to == null) to = from.plusWeeks(1);
        return ResponseEntity.ok(
            repository
                .findByOwnerAndTimeBetween(getOwner(principal), from.toInstant(), to.toInstant(), pageable)
                .map(dtoMapper::map)
        );
    }
}
