package de.iks.grocery_manager.server.controller.mdi;

import io.quarkus.rest.client.reactive.Url;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.RestHeader;
import org.jboss.resteasy.reactive.RestQuery;

@Path("/")
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
    @GET
    Response send(
        @Url String url,
        @RestHeader MultivaluedMap<String, String> headers,
        @RestQuery MultivaluedMap<String, String> params
    );
}
