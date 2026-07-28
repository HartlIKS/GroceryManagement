package de.iks.grocery_manager.server.mapping;

import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.mapstruct.TargetType;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CrudRepositoryMapper {
    @PersistenceContext
    private EntityManager em;

    public <T extends HasUUID> T map(UUID id, @TargetType Class<T> targetType) {
        return Optional.ofNullable(em.find(targetType, id)).orElseThrow();
    }
}
