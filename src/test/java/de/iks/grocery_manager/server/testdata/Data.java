package de.iks.grocery_manager.server.testdata;

public interface Data {
    @SqlResource("store.sql")
    interface Store {}
    @SqlResource("product.sql")
    interface Product {}
    @SqlResource("price.sql")
    interface Price extends Store, Product {}
    @SqlResource("product_group.sql")
    interface ProductGroup extends Product {}
    @SqlResource("shopping_list.sql")
    interface ShoppingList extends Product, ProductGroup {}
    @SqlResource("shopping_trip.sql")
    interface ShoppingTrip extends Product, Store {}
    @SqlResource("external_api.sql")
    interface ExternalApi extends Product, Store {}
    @SqlResource("product_endpoint.sql")
    interface ProductEndpoint extends ExternalApi {}
    @SqlResource("store_endpoint.sql")
    interface StoreEndpoint extends ExternalApi {}
    @SqlResource("price_endpoint.sql")
    interface PriceEndpoint extends ExternalApi {}
    @SqlResource("share.sql")
    interface Share {}
    @SqlResource("join_link.sql")
    interface JoinLink extends Share {}
}
