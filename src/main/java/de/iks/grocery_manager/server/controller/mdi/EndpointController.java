package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.controller.ParentedCRUDController;
import de.iks.grocery_manager.server.dto.PageDTO;
import de.iks.grocery_manager.server.dto.mdi.handling.RequestExecParams;
import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.jpa.mdi.EndpointRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.mdi.Endpoint;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Optional;
import java.util.UUID;

@Transactional
public abstract class EndpointController<E extends Endpoint, D extends HasUUID_DTO, C,
    R extends EndpointRepository<E> & BaseRepository<E>>
    extends ParentedCRUDController.Standard<E, D, C, R> {
    private final EntityMapper.Parented<E, D, C, C> dtoMapper;
    /*@RestClient
    private Receiver webClient;*/

    public EndpointController(
        R repository,
        EntityMapper.Parented<E, D, C, C> dtoMapper
    ) {
        super(repository, dtoMapper);
        this.dtoMapper = dtoMapper;
    }

    @GET
    public PageDTO<D> search(
        @PathParam("parentUuid") UUID parentUuid,
        @QueryParam("name") @DefaultValue("") String name,
        @QueryParam("page") @DefaultValue("0") int page,
        @QueryParam("size") @DefaultValue("10") int size
    ) {
        return DTOMapper.mapPage(
            repository
                .find(
                    "api.uuid = ?1 AND name LIKE '%' || ?2 || '%'",
                    parentUuid, name.replace("%", "%%")
                )
                .page(page, size),
            dtoMapper.map()
        );
    }

    /*@POST
    @Path("{uuid}/exec")
    public Response execute(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("parentUuid") UUID parentUuid,
        @PathParam("uuid") UUID uuid,
        RequestExecParams params
    ) {
        Optional<E> oEndpoint = repository.findByIdOptional(parentUuid, uuid);
        if(oEndpoint.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        E endpoint = oEndpoint.get();

        UriBuilder uriBuilder = UriBuilder.fromUri(endpoint.getBaseUrl());
        if(params.pathAppend() != null) uriBuilder = uriBuilder.path(params.pathAppend());
        return webClient.send(uriBuilder.toTemplate(), params.headers(), params.queryParams());
    }*/
}
