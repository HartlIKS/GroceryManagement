package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.controller.CRUDController;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.masterdata.CreatePriceListingDTO;
import de.iks.grocery_manager.server.dto.masterdata.ListPriceDTO;
import de.iks.grocery_manager.server.dto.masterdata.PriceListingDTO;
import de.iks.grocery_manager.server.dto.masterdata.UpdatePriceDTO;
import de.iks.grocery_manager.server.jpa.masterdata.PriceRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.model.masterdata.PriceListing;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.jboss.resteasy.reactive.RestResponse;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("/api/masterdata/price")
@Transactional
public class PriceListController
    extends CRUDController<PriceListing, ListPriceDTO, CreatePriceListingDTO, UpdatePriceDTO, PriceRepository> {
    private final DTOMapper dtoMapper;

    public PriceListController(
        PriceRepository repository,
        DTOMapper dtoMapper
    ) {
        super(repository, new EntityMapper<>(dtoMapper::map, dtoMapper::create, dtoMapper::update));
        this.dtoMapper = dtoMapper;
    }

    @GET
    public RestResponse<?> getOrSearchPrices(
        @QueryParam("at") Instant at,
        @QueryParam("products") Set<UUID> products,
        @QueryParam("stores") Set<UUID> stores,

        @QueryParam("store") UUID store,
        @QueryParam("product") UUID product,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        if(at != null) return RestResponse.ok(searchPrices(at, products, stores));
        else return RestResponse.ok(getPrices(store, product, page, size));
    }

    private PageDTO<ListPriceDTO> getPrices(
        UUID store,
        UUID product,
        int page,
        int size
    ) {
        PanacheQuery<PriceListing> query;
        if(store == null && product == null) {
            query = repository
                .findAll(Sort.by("uuid"));
        } else if(store == null) {
            query = repository
                .find("product.uuid", Sort.by("uuid"), product);
        } else if(product == null) {
            query = repository
                .find("store.uuid", Sort.by("uuid"), store);
        } else {
            query = repository
                .find("store.uuid = ?1 AND product.uuid = ?2", Sort.by("uuid"), store, product);
        }
        return dtoMapper.map(query.page(page, size), dtoMapper::map);
    }

    private Map<UUID, Map<UUID, List<PriceListingDTO>>> searchPrices(
        Instant at,
        Set<UUID> products,
        Set<UUID> stores
    ) {
        return repository
            .stream(
                ":at BETWEEN validFrom AND validTo AND store.uuid IN :stores AND product.uuid IN :products",
                Map.of("at", at, "stores", stores, "products", products)
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
            ));
    }
}
