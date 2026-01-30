package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateShoppingTripDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.ShoppingTripDTO;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
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

import java.time.ZonedDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(
    path = "/shoppingTrips",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@CrossOrigin("*")
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
        @RequestParam(required = false) ZonedDateTime from,
        @RequestParam(required = false) ZonedDateTime to,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal Object principal
    ) {
        if(from == null) from = ZonedDateTime.now();
        if(to == null) to = from.plusWeeks(1);
        return ResponseEntity.ok(
            trips
                .findByOwnerAndTimeBetween(getOwner(principal), from, to, pageable)
                .map(dtoMapper::map)
        );
    }
}
