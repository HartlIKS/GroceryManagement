package de.iks.grocery_manager.server;

import de.iks.grocery_manager.server.security.ShareSecurityIdentityAugmentor;
import io.quarkus.test.security.TestSecurity;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

@TestSecurity(user = "test", augmentors = ShareSecurityIdentityAugmentor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
@Inherited
public @interface WithTestUser {
    String OWNER = "user: test";
}
