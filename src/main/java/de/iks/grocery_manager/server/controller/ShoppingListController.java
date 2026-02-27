package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateShoppingListDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.ShoppingListDTO;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static de.iks.grocery_manager.server.util.OwnerUtils.getOwner;

@RequiredArgsConstructor
@RestController
@RequestMapping(
    path = "/api/shoppingLists",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ShoppingListController {
    private final ShoppingListRepository lists;
    private final ProductGroupRepository groups;
    private final ProductRepository products;
    private final DTOMapper dtoMapper;

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
    public void deleteShoppingList(
        @PathVariable UUID uuid,
        @RequestParam(required = false, defaultValue = "false") boolean ifNonRepeating,
        @AuthenticationPrincipal Object principal
    ) {
        if(ifNonRepeating) lists.deleteByUuidAndOwnerAndRepeatingIsFalse(uuid, getOwner(principal));
        else lists.deleteByUuidAndOwner(uuid, getOwner(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ShoppingListDTO> createShoppingList(
        @RequestBody CreateShoppingListDTO CreateShoppingListDTO,
        @AuthenticationPrincipal Object principal,
        UriComponentsBuilder uriBuilder
    ) {
        ShoppingListDTO ret = dtoMapper.map(lists.saveAndFlush(dtoMapper.create(
            CreateShoppingListDTO,
            getOwner(principal),
            products,
            groups
        )));
        return ResponseEntity
            .created(
                uriBuilder
                    .pathSegment("api", "shoppingLists", "{uuid}")
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
