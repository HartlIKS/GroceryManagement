package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.dto.JsonString;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.MappingHandler;
import de.iks.grocery_manager.server.model.HasUUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping(
    path = "/api/masterdata/interface/{uuid}/mapping",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public abstract class MappingTableController<
    E extends HasUUID,
    P extends JpaRepository<E, UUID>,
    E2 extends HasUUID,
    P2 extends JpaRepository<E2, UUID>
    > {
    protected final P repository;
    private final MappingHandler<E, E2> mappingHandler;
    protected final P2 mappedRepository;
    protected final DTOMapper dtoMapper;

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<UUID, String>> getMappings(
        @PathVariable UUID uuid
    ) {
        return ResponseEntity.of(
            repository.findById(uuid)
                .map(mappingHandler.getMappings())
                .map(dtoMapper::toUUIDMap)
        );
    }

    @GetMapping("/in/{remoteId}")
    @Transactional(readOnly = true)
    public ResponseEntity<UUID> translateInbound(
        @PathVariable UUID uuid,
        @PathVariable String remoteId
    ) {
        return mappingHandler.translateInbound()
            .apply(uuid, remoteId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> repository.existsById(uuid) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build());
    }

    @PutMapping("/in/{remoteId}")
    public ResponseEntity<UUID> setOutboundTranslation(
        @PathVariable UUID uuid,
        @PathVariable String remoteId,
        @RequestBody UUID localId
    ) {
        return ResponseEntity.of(
            mappedRepository
                .findById(localId)
                .flatMap(e -> {
                    Optional<@NonNull E> t = repository.findById(uuid);
                    t
                        .map(mappingHandler.getMappings())
                        .ifPresent(m -> m.put(e, remoteId));
                    return t
                        .map(repository::saveAndFlush)
                        .map(mappingHandler.getMappings())
                        .filter(m -> m.containsKey(e))
                        .map(ignored -> localId);
                })
        );
    }

    @GetMapping("/out/{localId}")
    @Transactional(readOnly = true)
    public ResponseEntity<String> translateOutbound(
        @PathVariable UUID uuid,
        @PathVariable UUID localId
    ) {
        return mappingHandler.translateOutbound()
            .apply(uuid, localId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> repository.existsById(uuid) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build());
    }

    @PutMapping("/out/{localId}")
    public ResponseEntity<String> setOutboundTranslation(
        @PathVariable UUID uuid,
        @PathVariable UUID localId,
        @RequestBody JsonString remoteId
    ) {
        return ResponseEntity.of(
            mappedRepository
                .findById(localId)
                .flatMap(e -> {
                    Optional<@NonNull E> t = repository.findById(uuid);
                    t
                        .map(mappingHandler.getMappings())
                        .ifPresent(m -> m.put(e, remoteId.str()));
                    return t
                        .map(repository::saveAndFlush)
                        .map(mappingHandler.getMappings())
                        .map(m -> m.get(e));
                })
        );
    }
}
