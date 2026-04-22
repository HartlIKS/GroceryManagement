package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.EntityMapper;
import de.iks.grocery_manager.server.dto.masterdata.CreateProductDTO;
import de.iks.grocery_manager.server.dto.masterdata.ListProductDTO;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.model.masterdata.Product;
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
    path = "/api/masterdata/product",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class ProductController extends CRUDController.Standard<Product, ListProductDTO, CreateProductDTO, ProductRepository> {
    private final DTOMapper dtoMapper;
    public ProductController(
        ProductRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "masterdata", "product", "{uuid}");
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ListProductDTO>> search(
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
