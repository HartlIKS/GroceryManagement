package de.iks.grocery_manager.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    public static final String AUTHORITY_MASTERDATA = "SCOPE_MASTERDATA";

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", corsConfiguration);
        return source;
    }

    @Bean
    public SecurityFilterChain masterDataFilterChain(HttpSecurity http) {
        return http
            .securityMatcher("/api/**")
            .cors(c -> corsConfigurationSource())
            .csrf(CsrfConfigurer::disable)
            .oauth2ResourceServer(o -> o
                .jwt(withDefaults())
            )
            .authorizeHttpRequests(r -> r
                .requestMatchers(HttpMethod.GET, "/api/masterdata/**")
                .authenticated()
                .requestMatchers("/api/masterdata/**")
                .hasAuthority(AUTHORITY_MASTERDATA)
                .anyRequest()
                .authenticated()
            )
            .build();
    }
}
