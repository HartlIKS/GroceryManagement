package de.iks.grocery_manager.server.mapping;

import de.iks.grocery_manager.server.dto.*;
import de.iks.grocery_manager.server.dto.masterdata.*;
import de.iks.grocery_manager.server.dto.mdi.*;
import de.iks.grocery_manager.server.dto.mdi.handling.*;
import de.iks.grocery_manager.server.dto.share.CreateJoinLinkDTO;
import de.iks.grocery_manager.server.dto.share.CreateShareDTO;
import de.iks.grocery_manager.server.dto.share.JoinLinkDTO;
import de.iks.grocery_manager.server.dto.share.ShareDTO;
import de.iks.grocery_manager.server.model.HasUUID;
import de.iks.grocery_manager.server.model.ProductGroup;
import de.iks.grocery_manager.server.model.ShoppingList;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Address;
import de.iks.grocery_manager.server.model.masterdata.PriceListing;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import de.iks.grocery_manager.server.model.mdi.*;
import de.iks.grocery_manager.server.model.mdi.handling.*;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Share;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;
import static org.mapstruct.SubclassExhaustiveStrategy.RUNTIME_EXCEPTION;

@Mapper(
    componentModel = SPRING,
    nullValuePropertyMappingStrategy = IGNORE,
    subclassExhaustiveStrategy = RUNTIME_EXCEPTION,
    uses = {
        CrudRepositoryMapper.Prices.class,
        CrudRepositoryMapper.Products.class,
        CrudRepositoryMapper.Stores.class,

        CrudRepositoryMapper.ProductGroups.class,
        CrudRepositoryMapper.ShoppingLists.class,
        CrudRepositoryMapper.ShoppingTrips.class,

        CrudRepositoryMapper.ExternalAPIs.class,
    }
)
public interface DTOMapper {
    default UUID toUUID(HasUUID entity) {
        return entity.getUuid();
    }

    ListStoreDTO map(Store store);

    @Mapping(target = "uuid", ignore = true)
    Store create(CreateStoreDTO store);

    @Mapping(target = "uuid", ignore = true)
    void update(@MappingTarget Store target, CreateStoreDTO update);

    ListProductDTO map(Product product);

    @Mapping(target = "uuid", ignore = true)
    Product create(CreateProductDTO product);

    @Mapping(target = "uuid", ignore = true)
    void update(@MappingTarget Product target, CreateProductDTO update);

    Map<Product, BigDecimal> toProducts(Map<UUID, BigDecimal> map);

    ListPriceDTO map(PriceListing price);

    @Mapping(target = "uuid", ignore = true)
    PriceListing create(CreatePriceListingDTO priceListingDTO);

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

    @Mapping(target = "permissions", expression = "java(share.getPermissionsFor(user))")
    ShareDTO map(Share share, @Context String user);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "links", expression = "java(new java.util.ArrayList<>())")
    Share create(CreateShareDTO shareDTO);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "links", ignore = true)
    void update(
        @MappingTarget Share target,
        CreateShareDTO update
    );

    default int toSize(Collection<?> collection) {
        return collection.size();
    }

    @Mapping(target = "numUsers", source = "users")
    JoinLinkDTO map(JoinLink link);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "users", expression = "java(new java.util.HashSet<>())")
    @Mapping(target = "name", source = "linkDTO.name")
    @Mapping(target = "use", ignore = true)
    JoinLink create(CreateJoinLinkDTO linkDTO, Share share);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "share", ignore = true)
    @Mapping(target = "users", ignore = true)
    @Mapping(target = "use", ignore = true)
    void update(
        @MappingTarget JoinLink target,
        CreateJoinLinkDTO update
    );

    List<JoinLinkDTO> map(List<JoinLink> links);

    default WrappedParameterDTO map2(Parameter parameter) {
        return new WrappedParameterDTO(map(parameter));
    }

    default Parameter create(WrappedParameterDTO parameterDTO) {
        return create(parameterDTO.parameter());
    }

    ParameterDTO map(Parameter parameter);

    Parameter create(ParameterDTO parameterDTO);

    void update(@MappingTarget Parameter target, ParameterDTO update);

    OneForAllDTO map(OneForAll oneForAll);

    OneForAll create(OneForAllDTO oneForAllDTO);

    @SubclassMapping(source = Parameter.class, target = WrappedParameterDTO.class)
    @SubclassMapping(source = Path.class, target = PathDTO.class)
    ProductHandlingDTO map(ProductHandling productHandling);

    @SubclassMapping(source = WrappedParameterDTO.class, target = Parameter.class)
    @SubclassMapping(source = PathDTO.class, target = Path.class)
    ProductHandling create(ProductHandlingDTO productHandlingDTO);

    @SubclassMapping(source = Parameter.class, target = WrappedParameterDTO.class)
    @SubclassMapping(source = Path.class, target = PathDTO.class)
    @SubclassMapping(source = OneForAll.class, target = OneForAllDTO.class)
    StoreHandlingDTO map(StoreHandling storeHandling);

    @SubclassMapping(source = WrappedParameterDTO.class, target = Parameter.class)
    @SubclassMapping(source = PathDTO.class, target = Path.class)
    @SubclassMapping(source = OneForAllDTO.class, target = OneForAll.class)
    StoreHandling create(StoreHandlingDTO storeHandlingDTO);

    ProductEndpointDTO map(ProductEndpoint endpoint);

    @Mapping(target = "uuid", ignore = true)
    ProductEndpoint create(CreateProductEndpointDTO createProductEndpointDTO, UUID api);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "api", ignore = true)
    void update(@MappingTarget ProductEndpoint target, CreateProductEndpointDTO update);

    AddressPathsDTO map(AddressPaths paths);

    AddressPaths map(AddressPathsDTO pathsDTO);

    void update(@MappingTarget AddressPaths target, AddressPathsDTO update);

    StoreEndpointDTO map(StoreEndpoint endpoint);

    @Mapping(target = "uuid", ignore = true)
    StoreEndpoint create(CreateStoreEndpointDTO createStoreEndpointDTO, UUID api);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "api", ignore = true)
    void update(@MappingTarget StoreEndpoint target, CreateStoreEndpointDTO update);

    PriceEndpointDTO map(PriceEndpoint endpoint);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "productHandlingType", ignore = true)
    @Mapping(target = "productPath", ignore = true)
    @Mapping(target = "productParameters", ignore = true)
    @Mapping(target = "storeHandlingType", ignore = true)
    @Mapping(target = "storePath", ignore = true)
    @Mapping(target = "storeParameters", ignore = true)
    PriceEndpoint create(CreatePriceEndpointDTO createPriceEndpointDTO, UUID api);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "api", ignore = true)
    @Mapping(target = "productHandlingType", ignore = true)
    @Mapping(target = "productPath", ignore = true)
    @Mapping(target = "productParameters", ignore = true)
    @Mapping(target = "storeHandlingType", ignore = true)
    @Mapping(target = "storePath", ignore = true)
    @Mapping(target = "storeParameters", ignore = true)
    void update(@MappingTarget PriceEndpoint target, CreatePriceEndpointDTO update);

    ExternalAPIDTO map(ExternalAPI api);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "endpoints", ignore = true)
    ExternalAPI create(CreateExternalAPIDTO createExternalAPIDTO);

    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "endpoints", ignore = true)
    void update(@MappingTarget ExternalAPI target, CreateExternalAPIDTO update);
}
