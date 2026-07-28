package de.iks.grocery_manager.server.controller.mdi;

import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;
import java.util.Map;

@RegisterRestClient(configKey = "any")
public interface Receiver {
    @Produces(
        {
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.APPLICATION_XHTML_XML,
            MediaType.TEXT_HTML,
            MediaType.TEXT_PLAIN
        }
    )
    Response send(
        @Url String url,
        @RestHeader Map<String, List<String>> headers,
        @RestQuery Map<String, List<String>> params
    );
}
