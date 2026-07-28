package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public abstract class CRUDController<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO, UpdateDTO,
    Repository extends BaseRepository<Entity>> {
    public static abstract class Standard<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO,
        Repository extends BaseRepository<Entity>>
        extends CRUDController<Entity, ListDTO, CreateDTO, CreateDTO, Repository> {
        public Standard(
            Repository repository,
            EntityMapper<Entity, ListDTO, CreateDTO, CreateDTO> dtoMapper
        ) {
            super(repository, dtoMapper);
        }
    }

    protected final Repository repository;
    private final EntityMapper<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper;
    @Inject
    protected UriInfo uriInfo;

    @GET
    @Path("{uuid}")
    public RestResponse<ListDTO> get(@PathParam("uuid") UUID uuid) {
        return repository
            .findByIdOptional(uuid)
            .map(dtoMapper.map())
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @PUT
    @Path("{uuid}")
    public RestResponse<ListDTO> put(@PathParam("uuid") UUID uuid, UpdateDTO updateDTO) {
        return repository
            .findByIdOptional(uuid)
            .map(s -> {
                dtoMapper
                    .update()
                    .accept(s, updateDTO);
                return repository.saveAndFlush(s);
            })
            .map(dtoMapper.map())
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @POST
    public RestResponse<ListDTO> create(CreateDTO createDTO) {
        ListDTO ret = dtoMapper
            .map()
            .apply(
                repository.saveAndFlush(
                    dtoMapper.create().apply(createDTO)
                )
            );
        return RestResponse.ResponseBuilder
            .create(Status.CREATED, ret)
            .location(
                uriInfo
                    .getAbsolutePathBuilder()
                    .path(ret.uuid().toString())
                    .build()
            )
            .build();
    }

    @DELETE
    @Path("{uuid}")
    public RestResponse<?> delete(@PathParam("uuid") UUID uuid) {
        repository.deleteById(uuid);
        return RestResponse
            .ok();
    }
}
