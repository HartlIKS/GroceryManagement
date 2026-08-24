package de.iks.grocery_manager.server.mapping;

import de.iks.grocery_manager.server.model.HasOwner;
import de.iks.grocery_manager.server.model.HasUUID;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public record EntityMapper<E extends HasUUID, DTO>(
    Function<E, DTO> map,
    Function<DTO, E> create,
    BiConsumer<E, DTO> update
) {
    public record Owned<E extends HasUUID & HasOwner, DTO>(
        Function<E, DTO> map,
        BiFunction<DTO, String, E> create,
        BiConsumer<E, DTO> update
    ) {
    }
    public record Parented<E extends HasUUID, DTO>(
        Function<E, DTO> map,
        BiFunction<DTO, UUID, E> create,
        BiConsumer<E, DTO> update
    ) {}
}
