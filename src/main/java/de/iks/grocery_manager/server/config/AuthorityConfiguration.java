package de.iks.grocery_manager.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.stream.Stream;

@Data
@Configuration
@ConfigurationProperties("spring.security.oauth2.resourceserver.scope")
public class AuthorityConfiguration {
    private String masterdata;

    public String getMasterdataAuthority() {
        return "SCOPE_"+masterdata;
    }

    public Stream<String> streamAllScopes() {
        return Stream.of(masterdata);
    }
}
