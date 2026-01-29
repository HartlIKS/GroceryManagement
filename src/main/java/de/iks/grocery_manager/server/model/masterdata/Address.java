package de.iks.grocery_manager.server.model.masterdata;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class Address {
    private String country;
    private String city;
    private String zip;
    private String street;
    private String number;
}