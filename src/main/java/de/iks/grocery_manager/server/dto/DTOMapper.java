package de.iks.grocery_manager.server.dto;

import de.iks.grocery_manager.server.dto.masterdata.*;
import de.iks.grocery_manager.server.jpa.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.HasUUID;
import de.iks.grocery_manager.server.model.ProductGroup;
import de.iks.grocery_manager.server.model.ShoppingList;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Address;
import de.iks.grocery_manager.server.model.masterdata.PriceListing;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(
    componentModel = ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = IGNORE,
    uses = {
        CrudRepositoryMapper.Prices.class,
        CrudRepositoryMapper.Products.class,
        CrudRepositoryMapper.Stores.class,

        CrudRepositoryMapper.ProductGroups.class,
        CrudRepositoryMapper.ShoppingLists.class,
        CrudRepositoryMapper.ShoppingTrips.class,
    }
)
public interface DTOMapper {
    ListStoreDTO map(Store store);

    default UUID toUUID(HasUUID entity) {
        return entity.getUuid();
    }

    @Mapping(target = "uuid", ignore = true)
    Store create(CreateStoreDTO store);

    @Mapping(target = "uuid", ignore = true)
    void update(@MappingTarget Store target, CreateStoreDTO update);

    ListProductDTO map(Product product);

    @Mapping(target = "uuid", ignore = true)
    Product create(CreateProductDTO product);

    @Mapping(target = "uuid", ignore = true)
    void update(@MappingTarget Product target, CreateProductDTO update);

    Map<UUID, BigDecimal> toProductUUIDs(Map<Product, BigDecimal> map);

    Map<Product, BigDecimal> toProducts(Map<UUID, BigDecimal> map);

    ListPriceDTO map(PriceListing price);

    @Mapping(target = ".", source = "priceListingDTO")
    @Mapping(target = "uuid", ignore = true)
    PriceListing create(CreatePriceListingDTO priceListingDTO, Store store, Product product);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "product", ignore = true)
    void update(@MappingTarget PriceListing target, UpdatePriceDTO update);

    @Mapping(target = "listPriceUUID", source = "uuid")
    PriceListingDTO map2(PriceListing priceListing);

    AddressDTO map(Address address);

    Address map(AddressDTO addressDTO);

    void update(@MappingTarget Address target, AddressDTO update);

    ListProductGroupDTO map(ProductGroup group);

    @Mapping(target = "uuid", ignore = true)
    ProductGroup create(CreateProductGroupDTO groupDTO, String owner);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void update(
        @MappingTarget ProductGroup target,
        CreateProductGroupDTO update
    );

    Map<UUID, BigDecimal> toGroupUUIDs(Map<ProductGroup, BigDecimal> map);

    Map<ProductGroup, BigDecimal> toGroups(Map<UUID, BigDecimal> map);

    ShoppingListDTO map(ShoppingList list);

    @Mapping(target = "uuid", ignore = true)
    ShoppingList create(
        CreateShoppingListDTO listDTO,
        String owner
    );

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void update(
        @MappingTarget ShoppingList target,
        CreateShoppingListDTO update
    );

    ShoppingTripDTO map(ShoppingTrip trip);

    @Mapping(target = "uuid", ignore = true)
    ShoppingTrip create(
        CreateShoppingTripDTO listDTO,
        String owner
    );

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void update(
        @MappingTarget ShoppingTrip target,
        CreateShoppingTripDTO update
    );
}
