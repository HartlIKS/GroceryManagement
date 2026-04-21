package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.mdi.CreateExternalAPIDTO;
import de.iks.grocery_manager.server.dto.mdi.ExternalAPIDTO;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
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
    path = "/api/masterdata/interface",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ExternalAPIController extends CRUDController.Standard<ExternalAPI, ExternalAPIDTO, CreateExternalAPIDTO, ExternalAPIRepository> {
    private final DTOMapper dtoMapper;
    public ExternalAPIController(
        ExternalAPIRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "masterdata", "interface", "{uuid}");
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ExternalAPIDTO>> search(
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(
            repository
                .findAllByNameContaining(name, pageable)
                .map(dtoMapper::map)
        );
    }
}
