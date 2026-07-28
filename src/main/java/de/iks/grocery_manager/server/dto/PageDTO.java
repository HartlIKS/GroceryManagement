package de.iks.grocery_manager.server.dto;

import java.util.List;

public record PageDTO<E>(
    List<? extends E> content,
    PageInfoDTO page
) {
}
