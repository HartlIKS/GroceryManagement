package de.iks.grocery_manager.server.util;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;

public class OwnerUtils {
    public static String getOwner(Object principal) {
        if(principal instanceof User u) return u.getUsername();
        if(principal instanceof Jwt j) return j.getSubject();
        if(principal instanceof String s) return s;
        throw new RuntimeException(String.format("Principal type not supported: %s", principal.getClass()));
    }
}
