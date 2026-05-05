package de.iks.grocery_manager.server.dto.mdi.handling;

import java.util.List;
import java.util.Map;

public record RequestExecParams(
    String pathAppend,
    Map<String, List<String>> queryParams,
    Map<String, List<String>> headers
) {
}
