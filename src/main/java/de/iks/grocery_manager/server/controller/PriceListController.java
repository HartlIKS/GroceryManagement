package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.*;
import de.iks.grocery_manager.server.jpa.PriceRepository;
import de.iks.grocery_manager.server.jpa.ProductRepository;
import de.iks.grocery_manager.server.jpa.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriBuilderFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping(
    path = "/price",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class PriceListController {
    private final ProductRepository products;
    private final StoreRepository stores;
    private final PriceRepository priceList;
    private final DTOMapper dtoMapper;
    private final UriBuilderFactory uriBuilderFactory;

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<ListPriceDTO> getPrice(@PathVariable UUID uuid) {
        return ResponseEntity.of(
            priceList
                .findById(uuid)
                .map(dtoMapper::map)
        );
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<ListPriceDTO> updatePrice(
        @PathVariable UUID uuid,
        @RequestBody UpdatePriceDTO updatePriceDTO
    ) {
        return ResponseEntity.of(
            priceList
                .findById(uuid)
                .map(p -> {
                    dtoMapper.update(p, updatePriceDTO);
                    return priceList.saveAndFlush(p);
                })
                .map(dtoMapper::map)
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ListPriceDTO> postPrice(@RequestBody CreatePriceListingDTO createPriceListingDTO) {
        return products
            .findById(createPriceListingDTO.product())
            .flatMap(product -> stores
                .findById(createPriceListingDTO.store())
                .map(store -> dtoMapper
                    .map(priceList.saveAndFlush(dtoMapper.create(
                        createPriceListingDTO,
                        store,
                        product
                    )))
                )
            )
            .map(ret -> ResponseEntity
                .created(
                    uriBuilderFactory
                        .builder()
                        .pathSegment("price", "{uuid}")
                        .build(ret.uuid())
                )
                .body(ret)
            )
            .orElseGet(ResponseEntity.notFound()::build);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deletePrice(@PathVariable UUID uuid) {
        priceList.deleteById(uuid);
        return ResponseEntity
            .ok()
            .build();
    }

    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<UUID, Map<UUID, List<PriceListingDTO>>>> getPrices(
        ZonedDateTime at,
        UUID[] products,
        UUID[] stores
    ) {
        return ResponseEntity.ok(
            priceList
                .findAllByValidFromBeforeAndValidToAfterAndStore_UuidInAndProduct_UuidIn(at, at, Set.of(stores), Set.of(products))
                .collect(Collectors.groupingBy(
                    p -> p
                        .getProduct()
                        .getUuid(),
                    Collectors.groupingBy(
                        p -> p
                            .getStore()
                            .getUuid(),
                        Collectors.mapping(dtoMapper::map2, Collectors.toList())
                    )
                ))
        );
    }
}
