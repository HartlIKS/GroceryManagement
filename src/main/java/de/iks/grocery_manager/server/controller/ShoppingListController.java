package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateShoppingListDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.EntityMapper.Owned;
import de.iks.grocery_manager.server.dto.ShoppingListDTO;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.model.ShoppingList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static de.iks.grocery_manager.server.util.OwnerUtils.getOwner;

@RestController
@RequestMapping(
    path = "/api/shoppingLists",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ShoppingListController extends OwnerTrackingCRUDController.Standard<ShoppingList, ShoppingListDTO, CreateShoppingListDTO, ShoppingListRepository> {
    private final DTOMapper dtoMapper;
    public ShoppingListController(
        ShoppingListRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new Owned<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "shoppingLists", "{uuid}");
        this.dtoMapper = dtoMapper;
    }

    @DeleteMapping(
        path = "/{uuid}",
        params = "ifNonRepeating"
    )
    public void delete(
        @PathVariable UUID uuid,
        @RequestParam boolean ifNonRepeating,
        @AuthenticationPrincipal Object principal
    ) {
        if(ifNonRepeating) repository.deleteByUuidAndOwnerAndRepeatingIsFalse(uuid, getOwner(principal));
        else repository.deleteByUuidAndOwner(uuid, getOwner(principal));
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ShoppingListDTO>> search(
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.ok(
            repository
                .findAllByOwnerAndNameContainingIgnoreCase(getOwner(principal), name, pageable)
                .map(dtoMapper::map)
        );
    }
}
