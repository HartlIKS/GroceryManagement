package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.security.ShareSecurityIdentityAugmentor;
import io.quarkus.test.security.TestSecurity;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@TestSecurity(
    user = "admin",
    roles = "MASTERDATA",
    augmentors = ShareSecurityIdentityAugmentor.class
)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
@Inherited
public @interface WithAdminUser {
}
