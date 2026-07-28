package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.jpa.share.ParentTrackingRepository;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public abstract class ParentedCRUDController<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO,
    UpdateDTO, Repository extends ParentTrackingRepository<Entity> & BaseRepository<Entity>> {
    public static abstract class Standard<Entity extends HasUUID, ListDTO extends HasUUID_DTO, CreateDTO,
        Repository extends ParentTrackingRepository<Entity> & BaseRepository<Entity>>
        extends
        ParentedCRUDController<Entity, ListDTO, CreateDTO, CreateDTO, Repository> {
        public Standard(
            Repository repository,
            EntityMapper.Parented<Entity, ListDTO, CreateDTO, CreateDTO> dtoMapper
        ) {
            super(repository, dtoMapper);
        }
    }

    protected final Repository repository;
    private final EntityMapper.Parented<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper;
    @Inject
    protected UriInfo uriInfo;

    @GET
    @Path("{uuid}")
    public RestResponse<ListDTO> get(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("parentUuid") UUID parentUuid,
        @PathParam("uuid") UUID uuid
    ) {
        return repository
            .findByIdOptional(parentUuid, uuid)
            .map(dtoMapper.map())
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @PUT
    @Path("{uuid}")
    public RestResponse<ListDTO> put(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("parentUuid") UUID parentUuid,
        @PathParam("uuid") UUID uuid,
        UpdateDTO updateDTO
    ) {
        return repository
            .findByIdOptional(parentUuid, uuid)
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
    @ResponseStatus(201)
    public RestResponse<ListDTO> create(
        @PathParam("parentUuid") UUID parentUuid,
        CreateDTO createDTO
    ) {
        ListDTO ret = dtoMapper
            .map()
            .apply(repository.saveAndFlush(dtoMapper
                                               .create()
                                               .apply(createDTO, parentUuid)));
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
    public RestResponse<?> delete(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("parentUuid") UUID parentUuid,
        @PathParam("uuid") UUID uuid
    ) {
        repository.deleteById(parentUuid, uuid);
        return RestResponse
            .ok();
    }
}
