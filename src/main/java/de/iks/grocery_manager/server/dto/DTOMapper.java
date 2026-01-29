package de.iks.grocery_manager.server.dto;

import de.iks.grocery_manager.server.dto.masterdata.*;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.model.*;
import de.iks.grocery_manager.server.model.masterdata.Address;
import de.iks.grocery_manager.server.model.masterdata.PriceListing;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import org.mapstruct.*;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.util.*;

import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(
    componentModel = ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = IGNORE
)
public interface DTOMapper {
    ListStoreDTO map(Store store);

    default UUID toUUID(Store store) {
        return store.getUuid();
    }

    @Mapping(target = "uuid", ignore = true)
    Store create(CreateStoreDTO store);

    @Mapping(target = "uuid", ignore = true)
    void update(@MappingTarget Store target, CreateStoreDTO update);

    ListProductDTO map(Product product);

    default UUID toUUID(Product product) {
        return product.getUuid();
    }

    default Product toProduct(UUID uuid, @Context ProductRepository repository) {
        return repository
            .findById(uuid)
            .orElseThrow(() -> new NoSuchElementException(uuid.toString()));
    }

    @Mapping(target = "uuid", ignore = true)
    Product create(CreateProductDTO product);

    @Mapping(target = "uuid", ignore = true)
    void update(@MappingTarget Product target, CreateProductDTO update);

    Map<UUID, BigDecimal> toProductUUIDs(Map<Product, BigDecimal> map);

    Map<Product, BigDecimal> toProducts(Map<UUID, BigDecimal> map, @Context ProductRepository repository);

    ListPriceDTO map(PriceListing price);

    @Mapping(target = ".", source = "priceListingDTO")
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "store", source = "store")
    @Mapping(target = "product", source = "product")
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

    default UUID toUUID(ProductGroup group) {
        return group.getUuid();
    }

    default ProductGroup toProductGroup(UUID uuid, @Context ProductGroupRepository repository) {
        return repository.findById(uuid)
            .orElseThrow(() -> new NoSuchElementException(uuid.toString()));
    }

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", source = "owner")
    ProductGroup create(CreateProductGroupDTO groupDTO, String owner, @Context ProductRepository repository);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void update(
        @MappingTarget ProductGroup target,
        CreateProductGroupDTO update,
        @Context ProductRepository repository
    );

    Map<UUID, BigDecimal> toGroupUUIDs(Map<ProductGroup, BigDecimal> map);

    Map<ProductGroup, BigDecimal> toGroups(Map<UUID, BigDecimal> map, @Context ProductGroupRepository repository);

    ShoppingListDTO map(ShoppingList list);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", source = "owner")
    ShoppingList create(
        CreateShoppingListDTO listDTO,
        String owner,
        @Context ProductRepository products,
        @Context ProductGroupRepository groups
    );

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void update(
        @MappingTarget ShoppingList target,
        CreateShoppingListDTO update,
        @Context ProductRepository products,
        @Context ProductGroupRepository groups
    );
}
