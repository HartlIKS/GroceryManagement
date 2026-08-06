package de.iks.grocery_manager.server.config;

import io.quarkus.vertx.http.security.CORS;
import io.quarkus.vertx.http.security.HttpSecurity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.HttpMethod;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@ApplicationScoped
public class SecurityConfiguration {
    public static final String AUTHORITY_READ = "READ_SCOPED";
    public static final String AUTHORITY_WRITE = "WRITE_SCOPED";
    public static final String AUTHORITY_ADMIN = "ADMIN_SCOPED";
    public static final String AUTHORITY_SHARE = "SHARE_SCOPED";
    public static final String AUTHORITY_USER = "USER_SCOPED";

    private CORS corsConfigurationSource() {
        return CORS
            .builder()
            .accessControlAllowCredentials()
            .origin("*")
            .returnExactOrigins(true)
            .header("*")
            .method("*")
            .build();
    }

    void apiFilterChain(@Observes HttpSecurity http) {
        http
            .cors(corsConfigurationSource())
            .path("/api/masterdata/*").methods(HttpMethod.GET, "QUERY").authenticated()
            .path("/api/masterdata/*").authorization().roles("MASTERDATA")
            .path("/api/share/current/links*").authorization().permissions(
                AUTHORITY_ADMIN,
                AUTHORITY_SHARE
            )
            .path("/api/share/current*").methods(HttpMethod.GET, "QUERY").authorization().permissions(
                AUTHORITY_SHARE
            )
            .path("/api/share/current*").authorization().permissions(
                AUTHORITY_ADMIN,
                AUTHORITY_SHARE
            )
            .path("/api/share*").authenticated()
            .path("/api*").methods(HttpMethod.GET, "QUERY").authorization().permissions(AUTHORITY_READ)
            .path("/api*").authorization().permissions(AUTHORITY_WRITE)
            .get("*").permit();
    }
}
