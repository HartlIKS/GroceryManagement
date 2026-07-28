package de.iks.grocery_manager.server;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
@Repeatable(Sql.Repeated.class)
public @interface Sql {
    String value();
    int order() default 0;
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Inherited
    @interface Repeated {
        Sql[] value() default {};
    }
}
