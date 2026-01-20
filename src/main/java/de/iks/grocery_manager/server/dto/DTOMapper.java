package de.iks.grocery_manager.server.dto;

import de.iks.grocery_manager.server.model.Address;
import de.iks.grocery_manager.server.model.PriceListing;
import de.iks.grocery_manager.server.model.Product;
import de.iks.grocery_manager.server.model.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.MappingTarget;

import java.util.UUID;

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

    @Mapping(target = "uuid", ignore = true)
    Product create(CreateProductDTO product);

    @Mapping(target = "uuid", ignore = true)
    void update(@MappingTarget Product target, CreateProductDTO update);

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
}
