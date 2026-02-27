package de.iks.grocery_manager.server.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Configuration
@ConfigurationProperties("frontend")
public class FrontendConfig {
    private final Map<String, String> auth = new HashMap<>();
}
