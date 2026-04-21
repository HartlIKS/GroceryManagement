package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.dto.masterdata.CreatePriceListingDTO;
import de.iks.grocery_manager.server.dto.masterdata.ListPriceDTO;
import de.iks.grocery_manager.server.dto.masterdata.PriceListingDTO;
import de.iks.grocery_manager.server.dto.masterdata.UpdatePriceDTO;
import de.iks.grocery_manager.server.jpa.masterdata.PriceRepository;
import de.iks.grocery_manager.server.model.masterdata.PriceListing;
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

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
    path = "/api/masterdata/price",
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Transactional
public class PriceListController extends CRUDController<PriceListing, ListPriceDTO, CreatePriceListingDTO, UpdatePriceDTO, PriceRepository> {
    private final DTOMapper dtoMapper;
    public PriceListController(
        PriceRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update), "api", "masterdata", "price", "{uuid}");
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<ListPriceDTO> getPrices(
        @RequestParam(required = false) UUID store,
        @RequestParam(required = false) UUID product,
        @PageableDefault Pageable page
    ) {
        if(store == null && product == null) {
            return repository
                .findAll(page)
                .map(dtoMapper::map);
        } else if(store == null) {
            return repository
                .findByProduct_Uuid(product, page)
                .map(dtoMapper::map);
        } else if(product == null) {
            return repository
                .findByStore_Uuid(store, page)
                .map(dtoMapper::map);
        } else {
            return repository
                .findByProduct_UuidAndStore_Uuid(product, store, page)
                .map(dtoMapper::map);
        }
    }

    @GetMapping(
        params = {"at"}
    )
    @Transactional(readOnly = true)
    public ResponseEntity<Map<UUID, Map<UUID, List<PriceListingDTO>>>> searchPrices(
        Instant at,
        UUID[] products,
        UUID[] stores
    ) {
        return ResponseEntity.ok(
            repository
                .findAllByValidFromLessThanEqualAndValidToGreaterThanEqualAndStore_UuidInAndProduct_UuidIn(
                    at,
                    at,
                    Set.of(stores),
                    Set.of(products)
                )
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
