package de.iks.grocery_manager.server.extra_http;

import jakarta.ws.rs.HttpMethod;

import java.lang.annotation.*;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@HttpMethod("QUERY")
public @interface QUERY {
}
