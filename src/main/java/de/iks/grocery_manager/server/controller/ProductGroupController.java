package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.CreateProductGroupDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.ListProductGroupDTO;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.ProductRepository;
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
    path = "/productGroups",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@CrossOrigin("*")
@Transactional
public class ProductGroupController {
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
        @AuthenticationPrincipal Object principal
    ) {
        ListProductGroupDTO ret = dtoMapper.map(groups.saveAndFlush(dtoMapper.create(
            createProductGroupDTO,
            getOwner(principal),
            products
        )));
        return ResponseEntity
            .created(
                uriBuilderFactory
                    .builder()
                    .pathSegment("productGroups", "{uuid}")
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
