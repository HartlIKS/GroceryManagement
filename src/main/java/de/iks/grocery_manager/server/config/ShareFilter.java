package de.iks.grocery_manager.server.config;

import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.UUID;

import static de.iks.grocery_manager.server.util.OwnerUtils.getUser;

@Component
@RequiredArgsConstructor
public class ShareFilter extends GenericFilterBean {
    public record SharePrincipal(Share share, Object principal) {
    }

    private final SecurityContextHolderStrategy contextHolder = SecurityContextHolder.getContextHolderStrategy();
    private final ShareRepository shares;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {
        String shareUuid = request.getParameter("share");
        final SecurityContext securityContext = contextHolder.getContext();
        final Authentication authentication = securityContext.getAuthentication();
        if(shareUuid != null) {
            if(authentication == null) throw new AuthenticationCredentialsNotFoundException("Authentication required");

            Share share = shares
                .findById(UUID.fromString(shareUuid))
                .orElseThrow(() -> new AccessDeniedException("No share with id " + shareUuid));

            Permissions permissions = share.getPermissionsFor(
                getUser(authentication.getPrincipal())
            );

            final SecurityContext newSecurityContext = contextHolder.createEmptyContext();

            newSecurityContext.setAuthentication(
                authentication
                    .toBuilder()
                    .principal(new SharePrincipal(share, authentication.getPrincipal()))
                    .authorities(c -> {
                        switch(permissions) {
                        case ADMIN:
                            c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_ADMIN));
                        case WRITE:
                            c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_WRITE));
                        case READ:
                            c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_READ));
                            c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_SHARE));
                        case NONE:
                            break;
                        }
                    })
                    .build()
            );

            contextHolder.setContext(newSecurityContext);
        } else if(authentication != null &&
            authentication.isAuthenticated() &&
            authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .noneMatch("ROLE_ANONYMOUS"::equals)
        ) {

            final SecurityContext newSecurityContext = contextHolder.createEmptyContext();

            newSecurityContext.setAuthentication(
                authentication
                    .toBuilder()
                    .authorities(c -> {
                        c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_ADMIN));
                        c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_WRITE));
                        c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_READ));
                        c.add(new SimpleGrantedAuthority(SecurityConfiguration.AUTHORITY_USER));
                    })
                    .build()
            );

            contextHolder.setContext(newSecurityContext);
        }
        chain.doFilter(request, response);
    }
}
