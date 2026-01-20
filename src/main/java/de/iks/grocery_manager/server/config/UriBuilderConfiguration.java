package de.iks.grocery_manager.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

@RequiredArgsConstructor
@Configuration
public class UriBuilderConfiguration {
    @Bean
    UriBuilderFactory uriBuilder() {
        return new DefaultUriBuilderFactory();
    }
}
