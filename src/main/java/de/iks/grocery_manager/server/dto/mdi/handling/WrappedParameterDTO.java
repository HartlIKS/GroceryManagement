package de.iks.grocery_manager.server.dto.mdi.handling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record WrappedParameterDTO(
    @JsonValue ParameterDTO parameter
) implements ProductHandlingDTO, StoreHandlingDTO {
    @JsonCreator
    public WrappedParameterDTO {}
}
