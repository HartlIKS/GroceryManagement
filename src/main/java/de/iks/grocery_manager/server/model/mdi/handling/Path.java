package de.iks.grocery_manager.server.model.mdi.handling;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Path implements ProductHandling, StoreHandling {
    private String path;
}
