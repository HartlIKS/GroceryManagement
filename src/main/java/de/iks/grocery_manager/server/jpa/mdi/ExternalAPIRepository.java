package de.iks.grocery_manager.server.jpa.mdi;

import de.iks.grocery_manager.server.mapping.CrudRepositoryMapper;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ExternalAPIRepository extends JpaRepository<ExternalAPI, UUID>, CrudRepositoryMapper.ExternalAPIs {
    @Query("SELECT key(m).uuid FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and value(m) = :remoteId")
    Optional<UUID> translateInboundProducts(UUID table, String remoteId);
    @Query("SELECT value(m) FROM ExternalAPI as api JOIN api.productMappings as m WHERE api.uuid = :table and key(m).uuid = :localId")
    Optional<String> translateOutboundProducts(UUID table, UUID localId);

    @Query("SELECT key(m).uuid FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and value(m) = :remoteId")
    Optional<UUID> translateInboundStores(UUID table, String remoteId);
    @Query("SELECT value(m) FROM ExternalAPI as api JOIN api.storeMappings as m WHERE api.uuid = :table and key(m).uuid = :localId")
    Optional<String> translateOutboundStores(UUID table, UUID localId);

    Page<ExternalAPI> findAllByNameContaining(String name, Pageable pageable);
}
