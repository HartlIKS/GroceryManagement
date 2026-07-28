package de.iks.grocery_manager.server.jpa.masterdata;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.masterdata.Store;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreRepository implements BaseRepository<Store> {
}
