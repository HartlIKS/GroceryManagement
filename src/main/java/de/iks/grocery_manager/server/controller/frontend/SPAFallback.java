package de.iks.grocery_manager.server.controller.frontend;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class SPAFallback {
    public void init(@Observes Router router) {
        router.get().last().handler(this::handle);
    }

    private void handle(RoutingContext ctx) {
        if(ctx.normalizedPath().startsWith("/api/") || ctx.normalizedPath().equals("/index.html")) {
            ctx.next();
            return;
        }
        ctx.reroute("/index.html");
    }
}
