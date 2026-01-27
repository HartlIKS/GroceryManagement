package de.iks.grocery_manager.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity(debug = true)
@ConditionalOnBooleanProperty(
    name = "secured",
    havingValue = false,
    matchIfMissing = true
)
public class NonSecurityConfiguration {
    @Bean
    public SecurityFilterChain publicFilterChain(HttpSecurity http) {
        return http
            .cors(withDefaults())
            .csrf(CsrfConfigurer::disable)
            .authorizeHttpRequests(r -> r
                .anyRequest()
                .permitAll()
            )
            .build();
    }
}
