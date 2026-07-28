package de.iks.grocery_manager.server.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.security.Principal;
import java.util.UUID;

@Getter
@RequestScoped
public class UserInfo {
    public static String getUser(SecurityIdentity securityIdentity) {
        Principal principal = securityIdentity.getPrincipal();
        if(principal instanceof JsonWebToken j) return String.format("sub: %s", j.getSubject());
        return String.format("user: %s", principal.getName());
    }

    private final String user;
    private final String owner;
    private final UUID shareId;

    public UserInfo(SecurityIdentity securityIdentity) {
        this.user = getUser(securityIdentity);
        this.shareId = securityIdentity.getAttribute("shareId");
        if(this.shareId == null) this.owner = this.user;
        else this.owner = String.format("share: %s", shareId);
    }
}
