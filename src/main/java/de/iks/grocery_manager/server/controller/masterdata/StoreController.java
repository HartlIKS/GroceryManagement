package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.dto.masterdata.CreateStoreDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.masterdata.ListStoreDTO;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilderFactory;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(
    path = "/masterdata/store", produces = MediaType.APPLICATION_JSON_VALUE
)
@CrossOrigin("*")
@Transactional
public class StoreController {
    private final StoreRepository stores;
    private final DTOMapper dtoMapper;
    private final UriBuilderFactory uriBuilderFactory;

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ListStoreDTO> getStore(@PathVariable UUID uuid) {
        return ResponseEntity.of(
            stores
                .findById(uuid)
                .map(dtoMapper::map)
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ListStoreDTO> putStore(@PathVariable UUID uuid, @RequestBody CreateStoreDTO store) {
        return ResponseEntity.of(
            stores
                .findById(uuid)
                .map(s -> {
                    dtoMapper.update(s, store);
                    return stores.saveAndFlush(s);
                })
                .map(dtoMapper::map)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ListStoreDTO> createStore(@RequestBody CreateStoreDTO createStoreDTO) {
        ListStoreDTO ret = dtoMapper.map(stores.saveAndFlush(dtoMapper.create(createStoreDTO)));
        return ResponseEntity
            .created(
                uriBuilderFactory
                    .builder()
                    .pathSegment("store", "{uuid}")
                    .build(ret.uuid())
            )
            .body(ret);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteStore(@PathVariable UUID uuid) {
        stores.deleteById(uuid);
        return ResponseEntity
            .ok()
            .build();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ListStoreDTO>> search(
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(
            stores
                .findAllByNameContainingIgnoreCase(name, pageable)
                .map(dtoMapper::map)
        );
    }
}
