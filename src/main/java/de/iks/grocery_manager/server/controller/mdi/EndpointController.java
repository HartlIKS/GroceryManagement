package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.controller.ParentedCRUDController;
import de.iks.grocery_manager.server.jpa.mdi.EndpointRepository;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.mdi.Endpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Transactional
public abstract class EndpointController<E extends Endpoint, D extends HasUUID_DTO, C, R extends EndpointRepository<E>> extends ParentedCRUDController.Standard<E, D, C, R> {
    private final EntityMapper.Parented<E, D, C, C> dtoMapper;
    public EndpointController(
        R repository,
        EntityMapper.Parented<E, D, C, C> dtoMapper,
        String... pathSegments
    ) {
        super(repository, dtoMapper, pathSegments);
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<D>> search(
        @PathVariable UUID parentUuid,
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(
            repository
                .findAllByApi_UuidAndNameContaining(parentUuid, name, pageable)
                .map(dtoMapper.map())
        );
    }
}
