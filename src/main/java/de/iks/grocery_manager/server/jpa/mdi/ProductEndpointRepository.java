package de.iks.grocery_manager.server.jpa.mdi;

import de.iks.grocery_manager.server.model.mdi.ProductEndpoint;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProductEndpointRepository implements EndpointRepository<ProductEndpoint> {
}
