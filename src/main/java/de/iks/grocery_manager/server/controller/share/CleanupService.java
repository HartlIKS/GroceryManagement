package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.jpa.OwnerTrackingJpaRepository;
import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

import java.util.List;

@ApplicationScoped
public class CleanupService {
    @Inject
    @All
    List<OwnerTrackingJpaRepository<?>> repositories;

    @Transactional(TxType.MANDATORY)
    public void deleteOwner(String owner) {
        repositories.forEach(r -> r.deleteAllByOwner(owner));
    }
}
