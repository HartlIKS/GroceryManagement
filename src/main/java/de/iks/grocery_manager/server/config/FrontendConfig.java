package de.iks.grocery_manager.server.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Configuration
@ConfigurationProperties("frontend")
public class FrontendConfig {
    private final Map<String, String> auth = new HashMap<>();

    @Getter(lazy = true) private final String authEtag = Integer.toString(auth.hashCode());

    public void applyPost(Optional<String> issuer, Stream<? extends String> customScopes) {
        issuer.ifPresent(s -> auth.putIfAbsent("issuer", s));
        auth.computeIfAbsent("scope", s -> Stream
            .concat(
                Stream.of("openid", "profile", "offline_access"),
                customScopes
            )
            .distinct()
            .collect(Collectors.joining(" "))
        );
    }
}
