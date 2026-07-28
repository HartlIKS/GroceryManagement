package de.iks.grocery_manager.server.jpa.masterdata;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.masterdata.Product;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductRepository implements BaseRepository<Product> {
}
