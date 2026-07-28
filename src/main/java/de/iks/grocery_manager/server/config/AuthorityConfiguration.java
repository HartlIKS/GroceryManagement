package de.iks.grocery_manager.server.config;

import io.smallrye.config.ConfigMapping;

import java.util.Optional;
import java.util.stream.Stream;

@ConfigMapping(prefix = "spring.security.oauth2.resourceserver.scope")
public interface AuthorityConfiguration {
    Optional<String> masterdata();

    default String getMasterdataAuthority() {
        return "SCOPE_"+masterdata().orElse("MASTERDATA");
    }

    default Stream<String> streamAllScopes() {
        return masterdata().stream();
    }
}
