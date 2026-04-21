package de.iks.grocery_manager.server.model.mdi;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class AddressPaths {
    private String countryPath;
    private String cityPath;
    private String zipPath;
    private String streetPath;
    private String numberPath;
}
