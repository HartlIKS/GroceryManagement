package de.iks.grocery_manager.server.controller.frontend;

import de.iks.grocery_manager.server.config.FrontendConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Path("/")
public class AuthData {
    private final Map<String, String> authMap;
    private final EntityTag authTag;
    private final CacheControl cacheControl = new CacheControl();

    public AuthData(
        FrontendConfig frontendConfig,
        @ConfigProperty(name = "quarkus.oidc.auth-server-url") Optional<String> authServer,
        @ConfigProperty(name = "quarkus.oidc.token.issuer") Optional<String> issuer
    ) {
        HashMap<String, String> authMap = new HashMap<>(frontendConfig.auth());
        authMap.computeIfAbsent("issuer", key -> issuer.or(() -> authServer).orElseThrow());
        this.authMap = Collections.unmodifiableMap(authMap);
        this.authTag = new EntityTag(Integer.toHexString(this.authMap.hashCode()));
        cacheControl.setMaxAge((int)TimeUnit.DAYS.toSeconds(1));
    }

    @GET
    @Path("/auth.json")
    public Response getAuthConfig(Request request) {
        var resp = request.evaluatePreconditions(authTag);
        if(resp != null) {
            return resp
                .cacheControl(cacheControl)
                .build();
        }
        return RestResponse.ResponseBuilder
            .ok(authMap)
            .tag(authTag)
            .cacheControl(cacheControl)
            .build()
            .toResponse();
    }
}