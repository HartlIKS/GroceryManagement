package de.iks.grocery_manager.server.dto.mdi.handling;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonTypeInfo(
    use = Id.NAME,
    include = As.PROPERTY,
    property = "type"
)
@JsonSubTypes({
    @Type(value = WrappedParameterDTO.class, name = "parameter"),
    @Type(value = PathDTO.class, name = "path"),
    @Type(value = OneForAllDTO.class, name = "oneForAll"),
})
public interface StoreHandlingDTO {
}
