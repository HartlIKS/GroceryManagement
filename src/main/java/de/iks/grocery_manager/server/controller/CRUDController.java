package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.HasUUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

@Transactional
public abstract class CRUDController<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO, UpdateDTO, Repository extends JpaRepository<Entity, UUID>> {
    public static abstract class Standard<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO, Repository extends JpaRepository<Entity, UUID>> extends CRUDController<Entity, ListDTO, CreateDTO, CreateDTO, Repository> {
        public Standard(
            Repository repository,
            EntityMapper<Entity, ListDTO, CreateDTO, CreateDTO> dtoMapper,
            String ...pathSegments
        ) {
            super(repository, dtoMapper, pathSegments);
        }
    }

    protected final Repository repository;
    private final EntityMapper<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper;
    private final String[] pathSegments;

    public CRUDController(
        Repository repository,
        EntityMapper<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper,
        String ...pathSegments
    ) {
        this.repository = repository;
        this.dtoMapper = dtoMapper;
        this.pathSegments = pathSegments;
    }

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ListDTO> get(@PathVariable UUID uuid) {
        return ResponseEntity.of(
            repository
                .findById(uuid)
                .map(dtoMapper.map())
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ListDTO> put(@PathVariable UUID uuid, @RequestBody UpdateDTO updateDTO) {
        return ResponseEntity.of(
            repository
                .findById(uuid)
                .map(s -> {
                    dtoMapper.update().accept(s, updateDTO);
                    return repository.saveAndFlush(s);
                })
                .map(dtoMapper.map())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ListDTO> create(@RequestBody CreateDTO createDTO, UriComponentsBuilder uriBuilder) {
        ListDTO ret = dtoMapper.map().apply(repository.saveAndFlush(dtoMapper.create().apply(createDTO)));
        return ResponseEntity
            .created(
                uriBuilder
                    .pathSegment(pathSegments)
                    .build(ret.uuid())
            )
            .body(ret);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> delete(@PathVariable UUID uuid) {
        repository.deleteById(uuid);
        return ResponseEntity
            .ok()
            .build();
    }
}
