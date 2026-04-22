package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.EntityMapper;
import de.iks.grocery_manager.server.dto.EntityMapper.Owned;
import de.iks.grocery_manager.server.dto.HasUUID_DTO;
import de.iks.grocery_manager.server.jpa.OwnerTrackingJpaRepository;
import de.iks.grocery_manager.server.model.HasOwner;
import de.iks.grocery_manager.server.model.HasUUID;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static de.iks.grocery_manager.server.util.OwnerUtils.getOwner;

@Transactional
public abstract class OwnerTrackingCRUDController<Entity extends HasUUID & HasOwner, ListDTO extends HasUUID_DTO, CreateDTO, UpdateDTO, Repository extends OwnerTrackingJpaRepository<@NonNull Entity>> {
    public static abstract class Standard<Entity extends HasUUID & HasOwner, ListDTO extends HasUUID_DTO, CreateDTO, Repository extends OwnerTrackingJpaRepository<@NonNull Entity>> extends OwnerTrackingCRUDController<Entity, ListDTO, CreateDTO, CreateDTO, Repository> {
        public Standard(
            Repository repository,
            Owned<Entity, ListDTO, CreateDTO, CreateDTO> dtoMapper,
            String ...pathSegments
        ) {
            super(repository, dtoMapper, pathSegments);
        }
    }

    protected final Repository repository;
    private final EntityMapper.Owned<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper;
    private final String[] pathSegments;

    public OwnerTrackingCRUDController(
        Repository repository,
        Owned<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper,
        String ...pathSegments
    ) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.pathSegments = pathSegments;
    }

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ListDTO> get(
        @PathVariable UUID uuid,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            repository
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(dtoMapper.map())
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ListDTO> update(
        @PathVariable UUID uuid,
        @RequestBody UpdateDTO createProductGroupDTO,
        @AuthenticationPrincipal Object principal
    ) {
        return ResponseEntity.of(
            repository
                .findByUuidAndOwner(uuid, getOwner(principal))
                .map(p -> {
                    dtoMapper.update().accept(
                        p,
                        createProductGroupDTO
                    );
                    return p;
                })
                .map(repository::saveAndFlush)
                .map(dtoMapper.map())
        );
    }

    @DeleteMapping("/{uuid}")
    public void delete(@PathVariable UUID uuid, @AuthenticationPrincipal Object principal) {
        repository.deleteByUuidAndOwner(uuid, getOwner(principal));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ListDTO> create(
        @RequestBody CreateDTO createProductGroupDTO,
        @AuthenticationPrincipal Object principal,
        UriComponentsBuilder uriBuilder
    ) {
        ListDTO ret = dtoMapper.map().apply(repository.saveAndFlush(dtoMapper.create().apply(
            createProductGroupDTO,
            getOwner(principal)
        )));
        return ResponseEntity
            .created(
                uriBuilder
                    .pathSegment(pathSegments)
                    .build(ret.uuid())
            )
            .body(ret);
    }
}
