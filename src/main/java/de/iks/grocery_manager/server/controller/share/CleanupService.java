package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.jpa.OwnerTrackingJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CleanupService {
    private final List<OwnerTrackingJpaRepository<?>> repositories;

    public void deleteOwner(String owner) {
        repositories.forEach(r -> r.deleteAllByOwner(owner));
    }
}
