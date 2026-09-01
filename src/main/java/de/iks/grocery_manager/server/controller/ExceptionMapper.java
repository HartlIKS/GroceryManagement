package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.dto.JsonString;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

public class ExceptionMapper {
    @ServerExceptionMapper
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleOptimisticLockException(OptimisticLockException e) {
        return Response.status(Status.CONFLICT).entity(new JsonString(e.getMessage())).build();
    }
}
