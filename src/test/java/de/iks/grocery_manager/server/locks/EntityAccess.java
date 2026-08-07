package de.iks.grocery_manager.server.locks;

import de.iks.grocery_manager.server.jpa.BaseRepository;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Target({METHOD, TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(EntityAccess.Repeat.class)
public @interface EntityAccess {
    @Target({METHOD, TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Repeat {
        EntityAccess[] value() default {};
    }
    Class<? extends BaseRepository<?>> value();
    boolean writes() default true;
}
