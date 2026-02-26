package de.iks.grocery_manager.server.jpa.masterdata;

import de.iks.grocery_manager.server.model.masterdata.PriceListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public interface PriceRepository extends JpaRepository<PriceListing, UUID> {
    Page<PriceListing> findByProduct_Uuid(UUID productUuid, Pageable pageable);
    Page<PriceListing> findByStore_Uuid(UUID storeUuid, Pageable pageable);
    Page<PriceListing> findByProduct_UuidAndStore_Uuid(UUID productUuid, UUID storeUuid, Pageable pageable);

    Stream<PriceListing> findAllByValidFromLessThanEqualAndValidToGreaterThanEqualAndStore_UuidInAndProduct_UuidIn(
        Instant validFromBefore,
        Instant validToAfter,
        Collection<? extends UUID> stores,
        Collection<? extends UUID> products
    );
}
