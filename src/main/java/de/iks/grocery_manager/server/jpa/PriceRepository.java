package de.iks.grocery_manager.server.jpa;

import de.iks.grocery_manager.server.model.PriceListing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public interface PriceRepository extends JpaRepository<PriceListing, UUID> {
    Stream<PriceListing> findAllByValidFromBeforeAndValidToAfterAndStore_UuidInAndProduct_UuidIn(
        ZonedDateTime validFromBefore,
        ZonedDateTime validToAfter,
        Collection<? extends UUID> stores,
        Collection<? extends UUID> products
    );
}
