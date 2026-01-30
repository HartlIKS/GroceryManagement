package de.iks.grocery_manager.server.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher.pathPattern;

@Configuration
@EnableWebSecurity
@ConditionalOnBooleanProperty(
    name = "secured"
)
public class SecurityConfiguration {
    public static final String AUTHORITY_MASTERDATA = "MASTERDATA";
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) {
        return http
            .securityMatcher(pathPattern(HttpMethod.GET, "/masterdata/**"))
            .cors(withDefaults())
            .authorizeHttpRequests(r -> r
                .anyRequest()
                .permitAll()
            )
            .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain masterDataFilterChain(HttpSecurity http) {
        return http
            .securityMatcher("/**")
            .cors(withDefaults())
            .csrf(CsrfConfigurer::disable)
            .oauth2ResourceServer(o -> o
                .jwt(withDefaults())
            )
            .authorizeHttpRequests(r -> r
                .requestMatchers("/masterdata/**")
                .hasAuthority(AUTHORITY_MASTERDATA)
                .requestMatchers("/productGroups/**", "/productGroups", "/shoppingLists/**", "/shoppingLists", "/shoppingTrips/**", "/shoppingTrips")
                .authenticated()
                .anyRequest()
                .denyAll()
            )
            .build();
    }
}
