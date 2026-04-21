package de.iks.grocery_manager.server.dto;

import de.iks.grocery_manager.server.model.HasOwner;
import de.iks.grocery_manager.server.model.HasUUID;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public record EntityMapper<E extends HasUUID, ListDTO, CreateDTO, UpdateDTO>(
    Function<E, ListDTO> map,
    Function<CreateDTO, E> create,
    BiConsumer<E, UpdateDTO> update
) {
    public record Owned<E extends HasUUID & HasOwner, ListDTO, CreateDTO, UpdateDTO>(
        Function<E, ListDTO> map,
        BiFunction<CreateDTO, String, E> create,
        BiConsumer<E, UpdateDTO> update
    ) {
    }
}
