package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.EntityMapper;
import de.iks.grocery_manager.server.dto.masterdata.CreateStoreDTO;
import de.iks.grocery_manager.server.dto.masterdata.ListStoreDTO;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.model.masterdata.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    path = "/api/masterdata/store", produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class StoreController extends CRUDController.Standard<Store, ListStoreDTO, CreateStoreDTO, StoreRepository> {
    private final DTOMapper dtoMapper;
    public StoreController(StoreRepository repository, DTOMapper dtoMapper) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "masterdata", "store", "{uuid}");
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ListStoreDTO>> search(
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(
            repository
                .findAllByNameContainingIgnoreCase(name, pageable)
                .map(dtoMapper::map)
        );
    }
}
