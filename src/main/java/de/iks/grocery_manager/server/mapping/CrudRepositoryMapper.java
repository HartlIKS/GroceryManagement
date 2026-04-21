package de.iks.grocery_manager.server.mapping;

import de.iks.grocery_manager.server.model.ProductGroup;
import de.iks.grocery_manager.server.model.ShoppingList;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.PriceListing;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import de.iks.grocery_manager.server.model.mdi.*;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public interface CrudRepositoryMapper<T, ID> {
    interface Prices extends CrudRepositoryMapper<PriceListing, UUID> {}
    interface Products extends CrudRepositoryMapper<Product, UUID> {}
    interface Stores extends CrudRepositoryMapper<Store, UUID> {}
    interface ProductGroups extends CrudRepositoryMapper<ProductGroup, UUID> {}
    interface ShoppingLists extends CrudRepositoryMapper<ShoppingList, UUID> {}
    interface ShoppingTrips extends CrudRepositoryMapper<ShoppingTrip, UUID> {}
    interface ExternalAPIs extends CrudRepositoryMapper<ExternalAPI, UUID> {}

    Optional<T> findById(ID id);
    default T map(ID id) {
        return findById(id)
            .orElseThrow(() -> new NoSuchElementException(Objects.toString(id)));
    }
}
