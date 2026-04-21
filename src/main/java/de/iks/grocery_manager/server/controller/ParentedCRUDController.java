package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.jpa.share.ParentTrackingRepository;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.HasUUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@SuppressWarnings("MVCPathVariableInspection")
@Transactional
public abstract class ParentedCRUDController<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO, UpdateDTO, Repository extends ParentTrackingRepository<Entity>> {
    public static abstract class Standard<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO, Repository extends ParentTrackingRepository<Entity>> extends
        ParentedCRUDController<Entity, ListDTO, CreateDTO, CreateDTO, Repository> {
        public Standard(
            Repository repository,
            EntityMapper.Parented<Entity, ListDTO, CreateDTO, CreateDTO> dtoMapper,
            String ...pathSegments
        ) {
            super(repository, dtoMapper, pathSegments);
        }
    }

    protected final Repository repository;
    private final EntityMapper.Parented<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper;
    private final String[] pathSegments;

    public ParentedCRUDController(
        Repository repository,
        EntityMapper.Parented<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper,
        String ...pathSegments
    ) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.pathSegments = pathSegments;
    }

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ListDTO> get(@PathVariable UUID parentUuid, @PathVariable UUID uuid) {
        return ResponseEntity.of(
            repository
                .findById(parentUuid, uuid)
                .map(dtoMapper.map())
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ListDTO> put(@PathVariable UUID parentUuid, @PathVariable UUID uuid, @RequestBody UpdateDTO updateDTO) {
        return ResponseEntity.of(
            repository
                .findById(parentUuid, uuid)
                .map(s -> {
                    dtoMapper.update().accept(s, updateDTO);
                    return repository.saveAndFlush(s);
                })
                .map(dtoMapper.map())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ListDTO> create(@PathVariable UUID parentUuid, @RequestBody CreateDTO createDTO, UriComponentsBuilder uriBuilder) {
        ListDTO ret = dtoMapper.map().apply(repository.saveAndFlush(dtoMapper.create().apply(createDTO, parentUuid)));
        return ResponseEntity
            .created(
                uriBuilder
                    .pathSegment(pathSegments)
                    .build(parentUuid, ret.uuid())
            )
            .body(ret);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID parentUuid, @PathVariable UUID uuid) {
        repository.deleteById(parentUuid, uuid);
        return ResponseEntity
            .ok()
            .build();
    }
}
