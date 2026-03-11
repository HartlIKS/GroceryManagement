package de.iks.grocery_manager.server.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import static org.springframework.security.config.Customizer.withDefaults;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    public static final String AUTHORITY_READ = "READ_SCOPED";
    public static final String AUTHORITY_WRITE = "WRITE_SCOPED";
    public static final String AUTHORITY_ADMIN = "ADMIN_SCOPED";
    public static final String AUTHORITY_SHARE = "SHARE_SCOPED";
    public static final String AUTHORITY_USER = "USER_SCOPED";
    private final AuthorityConfiguration authorityConfiguration;

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
    public SecurityFilterChain apiFilterChain(HttpSecurity http, ShareFilter shareFilter) {
        return http
            .securityMatcher("/api/**")
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .csrf(CsrfConfigurer::disable)
            .oauth2ResourceServer(o -> o
                .jwt(withDefaults())
            )
            .addFilterBefore(shareFilter, AuthorizationFilter.class)
            .authorizeHttpRequests(r -> r
                .requestMatchers(HttpMethod.GET, "/api/masterdata/**")
                .authenticated()
                .requestMatchers("/api/masterdata/**")
                .hasAuthority(authorityConfiguration.getMasterdataAuthority())
                .requestMatchers("/api/share/current/links", "/api/share/current/links/**")
                .hasAllAuthorities(AUTHORITY_ADMIN, AUTHORITY_SHARE)
                .requestMatchers(HttpMethod.GET, "/api/share/current", "/api/share/current/**")
                .hasAuthority(AUTHORITY_SHARE)
                .requestMatchers("/api/share/current", "/api/share/current/**")
                .hasAllAuthorities(AUTHORITY_ADMIN, AUTHORITY_SHARE)
                .requestMatchers("/api/share", "/api/share/**")
                .authenticated()
                .requestMatchers(HttpMethod.GET)
                .hasAuthority(AUTHORITY_READ)
                .anyRequest()
                .hasAuthority(AUTHORITY_WRITE)
            )
            .build();
    }
}
