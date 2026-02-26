package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateShoppingTripDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.ShoppingTripDTO;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.model.masterdata.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilderFactory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(
    path = "/shoppingTrips",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ShoppingTripController {
    private final ShoppingTripRepository trips;
    private final StoreRepository stores;
    private final ProductRepository products;
    private final DTOMapper dtoMapper;
    private final UriBuilderFactory uriBuilderFactory;

    private String getOwner(Object principal) {
        return switch(principal) {
            case User u -> u.getUsername();
            case Jwt j -> j.getSubject();
            case String s -> s;
            case null -> "";
            default ->
                throw new RuntimeException(String.format("Principal type not supported: %s", principal.getClass()));
        };
    }

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ShoppingTripDTO> getShoppingTrip(
        @PathVariable UUID uuid,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            trips
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(dtoMapper::map)
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ShoppingTripDTO> updateShoppingTrip(
        @PathVariable UUID uuid,
        @RequestBody CreateShoppingTripDTO createShoppingTripDTO,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            trips
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(p -> {
                    dtoMapper.update(
                        p,
                        createShoppingTripDTO,
                        stores,
                        products
                    );
                    return p;
                })
                .map(trips::saveAndFlush)
                .map(dtoMapper::map)
        );
    }

    @PostMapping("/{uuid}/add")
    public ResponseEntity<ShoppingTripDTO> addToShoppingTrip(
        @PathVariable UUID uuid,
        @RequestBody Map<UUID, BigDecimal> products,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            trips
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(p -> {
                    Map<Product, BigDecimal> prods = p.getProducts();
                    products.forEach((product, amount) -> prods.merge(
                        dtoMapper.toProduct(product, this.products),
                        amount,
                        BigDecimal::add
                    ));
                    return p;
                })
                .map(trips::saveAndFlush)
                .map(dtoMapper::map)
        );
    }

    @DeleteMapping("/{uuid}")
    public void deleteShoppingTrip(@PathVariable UUID uuid, @AuthenticationPrincipal Object principal) {
        trips.deleteByUuidAndOwner(uuid, getOwner(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ShoppingTripDTO> createShoppingTrip(
        @RequestBody CreateShoppingTripDTO CreateShoppingTripDTO,
        @AuthenticationPrincipal Object principal
    ) {
        ShoppingTripDTO ret = dtoMapper.map(trips.saveAndFlush(dtoMapper.create(
            CreateShoppingTripDTO,
            getOwner(principal),
            stores,
            products
        )));
        return ResponseEntity
            .created(
                uriBuilderFactory
                    .builder()
                    .pathSegment("shoppingLists", "{uuid}")
                    .build(ret.uuid())
            )
            .body(ret);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ShoppingTripDTO>> search(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal Object principal
    ) {
        if(from == null) from = Instant.now();
        if(to == null) to = from.plus(1, ChronoUnit.WEEKS);
        return ResponseEntity.ok(
            trips
                .findByOwnerAndTimeBetween(getOwner(principal), from, to, pageable)
                .map(dtoMapper::map)
        );
    }
}
