package de.iks.grocery_manager.server.util;

import de.iks.grocery_manager.server.config.ShareFilter.SharePrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;

public class OwnerUtils {
    public static String getOwner(Object principal) {
        if(principal instanceof SharePrincipal s) return String.format("share: %s", s.share().getUuid());
        if(principal instanceof User u) return String.format("user: %s", u.getUsername());
        if(principal instanceof Jwt j) return String.format("sub: %s", j.getSubject());
        if(principal instanceof String s) return String.format("raw: %s", s);
        throw new RuntimeException(String.format("Principal type not supported: %s", principal.getClass()));
    }

    public static String getUser(Object principal) {
        if(principal instanceof SharePrincipal s) return getUser(s.principal());
        else return getOwner(principal);
    }
}
