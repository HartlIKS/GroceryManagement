package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.*;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
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

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(
    path = "/shoppingLists",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@CrossOrigin("*")
@Transactional
public class ShoppingListController {
    private final ShoppingListRepository lists;
    private final ProductGroupRepository groups;
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
    public ResponseEntity<ShoppingListDTO> getShoppingList(
        @PathVariable UUID uuid,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            lists
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(dtoMapper::map)
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ShoppingListDTO> updateShoppingList(
        @PathVariable UUID uuid,
        @RequestBody CreateShoppingListDTO createShoppingListDTO,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            lists
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(p -> {
                    dtoMapper.update(
                        p,
                        createShoppingListDTO,
                        products,
                        groups
                    );
                    return p;
                })
                .map(lists::saveAndFlush)
                .map(dtoMapper::map)
        );
    }

    @DeleteMapping("/{uuid}")
    public void deleteShoppingList(@PathVariable UUID uuid, @AuthenticationPrincipal Object principal) {
        lists.deleteByUuidAndOwner(uuid, getOwner(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ShoppingListDTO> createShoppingList(
        @RequestBody CreateShoppingListDTO CreateShoppingListDTO,
        @AuthenticationPrincipal Object principal
    ) {
        ShoppingListDTO ret = dtoMapper.map(lists.saveAndFlush(dtoMapper.create(
            CreateShoppingListDTO,
            getOwner(principal),
            products,
            groups
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
    public ResponseEntity<Page<ShoppingListDTO>> search(
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.ok(
            lists
                .findAllByOwnerAndNameContainingIgnoreCase(getOwner(principal), name, pageable)
                .map(dtoMapper::map)
        );
    }
}
