package de.iks.grocery_manager.server.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record JsonString(
    @JsonValue
    String str
) {
    @JsonCreator
    public JsonString {
    }
}
