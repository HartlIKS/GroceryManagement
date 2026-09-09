package de.iks.grocery_manager.server.testdata;

import de.iks.rtrm.InitializeWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@InitializeWith(SqlInitializer.class)
public @interface SqlResource {
    String value();
}
