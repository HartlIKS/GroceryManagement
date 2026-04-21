package de.iks.grocery_manager.server.model.mdi.handling;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Parameter implements ProductHandling, StoreHandling {
    private String header;
    private String queryParameter;
}
