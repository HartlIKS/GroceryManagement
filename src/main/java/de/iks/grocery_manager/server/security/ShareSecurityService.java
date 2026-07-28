package de.iks.grocery_manager.server.security;

import de.iks.grocery_manager.server.config.SecurityConfiguration;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@ApplicationScoped
public class ShareSecurityService {
    private final ShareRepository shareRepository;

    @ActivateRequestContext
    public SecurityIdentity augment(SecurityIdentity identity, RoutingContext ctx) {
        if(identity.isAnonymous()) return identity;
        List<String> shareIds = ctx != null ? ctx.queryParam("share") : List.of();
        if(shareIds.isEmpty()) return QuarkusSecurityIdentity
            .builder(identity)
            .addPermissionsAsString(Set.of(
                SecurityConfiguration.AUTHORITY_READ,
                SecurityConfiguration.AUTHORITY_WRITE,
                SecurityConfiguration.AUTHORITY_ADMIN,
                SecurityConfiguration.AUTHORITY_USER
            ))
            .build();
        if(shareIds.size() != 1) throw new BadRequestException("Multiple share IDs specified");
        UUID shareId = UUID.fromString(shareIds.get(0));
        var builder = QuarkusSecurityIdentity.builder(identity);
        switch(shareRepository.getPermissionsForUser(shareId, UserInfo.getUser(identity))) {
        case ADMIN:
            builder.addPermissionAsString(SecurityConfiguration.AUTHORITY_ADMIN);
        case WRITE:
            builder.addPermissionAsString(SecurityConfiguration.AUTHORITY_WRITE);
        case READ:
            builder.addPermissionAsString(SecurityConfiguration.AUTHORITY_READ);
            builder.addPermissionAsString(SecurityConfiguration.AUTHORITY_SHARE);
            builder.addAttribute("shareId", shareId);
            break;
        }
        return builder.build();
    }
}
