package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateProductGroupDTO;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper.Owned;
import de.iks.grocery_manager.server.dto.ListProductGroupDTO;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.model.ProductGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static de.iks.grocery_manager.server.util.OwnerUtils.getOwner;

@RestController
@RequestMapping(
    path = "/api/productGroups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ProductGroupController extends OwnerTrackingCRUDController.Standard<ProductGroup, ListProductGroupDTO, CreateProductGroupDTO, ProductGroupRepository> {
    private final DTOMapper dtoMapper;
    public ProductGroupController(
        ProductGroupRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new Owned<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "productGroups", "{uuid}");
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ListProductGroupDTO>> search(
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
