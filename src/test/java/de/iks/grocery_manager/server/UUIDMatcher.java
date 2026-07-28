package de.iks.grocery_manager.server;

import de.iks.grocery_manager.server.model.HasUUID;
import org.hamcrest.Matcher;

import java.util.UUID;

import static org.hamcrest.Matchers.is;

public class UUIDMatcher {
    public static Matcher<?> isUuidOf(HasUUID source) {
        return isUuid(source.getUuid());
    }
    public static Matcher<?> isUuid(UUID uuid) {
        return is(uuid.toString());
    }
}
