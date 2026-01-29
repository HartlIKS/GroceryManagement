package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.dto.masterdata.CreateProductDTO;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.masterdata.ListProductDTO;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
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
    path = "/masterdata/product",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@CrossOrigin("*")
@Transactional
public class ProductController {
    private final ProductRepository products;
    private final DTOMapper dtoMapper;
    private final UriBuilderFactory uriBuilderFactory;

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ListProductDTO> getProduct(@PathVariable UUID uuid) {
        return ResponseEntity.of(
            products
                .findById(uuid)
                .map(dtoMapper::map)
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ListProductDTO> updateProduct(
        @PathVariable UUID uuid,
        @RequestBody CreateProductDTO createProductDTO
    ) {
        return ResponseEntity.of(
            products
                .findById(uuid)
                .map(p -> {
                    dtoMapper.update(
                        p,
                        createProductDTO
                    );
                    return products.saveAndFlush(p);
                })
                .map(dtoMapper::map)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ListProductDTO> createProduct(@RequestBody CreateProductDTO createProductDTO) {
        ListProductDTO ret = dtoMapper.map(products.saveAndFlush(dtoMapper.create(createProductDTO)));
        return ResponseEntity
            .created(
                uriBuilderFactory
                    .builder()
                    .pathSegment("product", "{uuid}")
                    .build(ret.uuid())
            )
            .body(ret);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteProduct(@PathVariable UUID uuid) {
        products.deleteById(uuid);
        return ResponseEntity
            .ok()
            .build();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Page<ListProductDTO>> search(
        @RequestParam(defaultValue = "") String name,
        @PageableDefault Pageable pageable
    ) {
        return ResponseEntity.ok(
            products
                .findAllByNameContainingIgnoreCase(name, pageable)
                .map(dtoMapper::map)
        );
    }
}
