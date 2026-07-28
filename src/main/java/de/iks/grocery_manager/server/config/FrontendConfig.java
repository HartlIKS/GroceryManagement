package de.iks.grocery_manager.server.config;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = "frontend")
public interface FrontendConfig {
    Map<String, String> auth();
}
