package de.iks.grocery_manager.server.jpa.masterdata;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.masterdata.PriceListing;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

@ApplicationScoped
public class PriceRepository implements BaseRepository<PriceListing> {
}
