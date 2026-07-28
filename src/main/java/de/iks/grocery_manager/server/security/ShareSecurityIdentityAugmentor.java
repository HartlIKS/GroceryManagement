package de.iks.grocery_manager.server.security;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.SecurityIdentityAugmentor;
import io.quarkus.vertx.http.runtime.security.HttpSecurityUtils;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;

@ApplicationScoped
public class ShareSecurityIdentityAugmentor implements SecurityIdentityAugmentor {
    @Inject
    ShareSecurityService shareSecurityService;

    @Override
    public Uni<SecurityIdentity> augment(SecurityIdentity identity, AuthenticationRequestContext context) {
        return context.runBlocking(() -> shareSecurityService.augment(identity, HttpSecurityUtils.getRoutingContextAttribute(identity)));
    }

    @Override
    public Uni<SecurityIdentity> augment(
        SecurityIdentity identity,
        AuthenticationRequestContext context,
        Map<String, Object> attributes
    ) {
        return context.runBlocking(() -> shareSecurityService.augment(identity, HttpSecurityUtils.getRoutingContextAttribute(attributes)));
    }
}
