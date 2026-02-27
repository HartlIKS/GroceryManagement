package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateProductGroupDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.ListProductGroupDTO;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
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
    path = "/api/productGroups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ProductGroupController {
    private final ProductGroupRepository groups;
    private final ProductRepository products;
    private final DTOMapper dtoMapper;

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ListProductGroupDTO> getProductGroup(
        @PathVariable UUID uuid,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            groups
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(dtoMapper::map)
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ListProductGroupDTO> updateProductGroup(
        @PathVariable UUID uuid,
        @RequestBody CreateProductGroupDTO createProductGroupDTO,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            groups
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(p -> {
                    dtoMapper.update(
                        p,
                        createProductGroupDTO,
                        products
                    );
                    return p;
                })
                .map(groups::saveAndFlush)
                .map(dtoMapper::map)
        );
    }

    @DeleteMapping("/{uuid}")
    public void deleteProductGroup(@PathVariable UUID uuid, @AuthenticationPrincipal Object principal) {
        groups.deleteByUuidAndOwner(uuid, getOwner(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ListProductGroupDTO> createProductGroup(
        @RequestBody CreateProductGroupDTO createProductGroupDTO,
        @AuthenticationPrincipal Object principal,
        UriComponentsBuilder uriBuilder
    ) {
        ListProductGroupDTO ret = dtoMapper.map(groups.saveAndFlush(dtoMapper.create(
            createProductGroupDTO,
            getOwner(principal),
            products
        )));
        return ResponseEntity
            .created(
                uriBuilder
                    .pathSegment("api", "productGroups", "{uuid}")
                    .build(ret.uuid())
            )
            .body(ret);
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ListProductGroupDTO>> search(
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.ok(
            groups
                .findAllByOwnerAndNameContainingIgnoreCase(getOwner(principal), name, pageable)
                .map(dtoMapper::map)
        );
    }
}
