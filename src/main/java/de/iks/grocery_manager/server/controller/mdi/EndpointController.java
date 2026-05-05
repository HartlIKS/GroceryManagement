package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.controller.ParentedCRUDController;
import de.iks.grocery_manager.server.dto.mdi.handling.RequestExecParams;
import de.iks.grocery_manager.server.jpa.mdi.EndpointRepository;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.mdi.Endpoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@RequestMapping("/api/masterdata/interface/{parentUuid}/endpoint")
@Transactional
public abstract class EndpointController<E extends Endpoint, D extends HasUUID_DTO, C, R extends EndpointRepository<E>> extends ParentedCRUDController.Standard<E, D, C, R> {
    private final EntityMapper.Parented<E, D, C, C> dtoMapper;
    private final RestTemplate restTemplate;
    public EndpointController(
        R repository,
        EntityMapper.Parented<E, D, C, C> dtoMapper,
        RestTemplate restTemplate,
        String... pathSegments
    ) {
        super(repository, dtoMapper, pathSegments);
        this.dtoMapper = dtoMapper;
        this.restTemplate = restTemplate;
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

    @PostMapping("{uuid}/exec")
    @Transactional(readOnly = true)
    public ResponseEntity<String> execute(
        @PathVariable UUID parentUuid,
        @PathVariable UUID uuid,
        @RequestBody RequestExecParams params
    ) {
        Optional<E> oEndpoint = repository.findById(parentUuid, uuid);
        if (oEndpoint.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        E endpoint = oEndpoint.get();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(endpoint.getBaseUrl());
        if(params.pathAppend() != null) uriBuilder = uriBuilder.pathSegment(params.pathAppend());
        for(var e : Objects.<Map<String, List<String>>>requireNonNullElseGet(params.queryParams(), Map::of).entrySet()) {
            uriBuilder = uriBuilder.queryParam(e.getKey(), e.getValue());
        }
        var request = RequestEntity
            .get(uriBuilder.build().toUri())
            .headers(h -> Objects.<Map<String, List<String>>>requireNonNullElseGet(params.headers(), Map::of).forEach(h::addAll))
            .build();
        ResponseEntity<String> resp = restTemplate.exchange(request, String.class);
        resp.getHeaders().clear();
        return resp;
    }
}
