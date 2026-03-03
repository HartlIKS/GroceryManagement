package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CleanupService {
    private final ProductGroupRepository groups;
    private final ShoppingListRepository lists;
    private final ShoppingTripRepository trips;

    public void deleteOwner(String owner) {
        trips.deleteAllByOwner(owner);
        lists.deleteAllByOwner(owner);
        groups.deleteAllByOwner(owner);
    }
}
