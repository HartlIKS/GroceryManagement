package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.jpa.OwnerTrackingJpaRepository;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.EntityMapper.Owned;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.HasOwner;
import de.iks.grocery_manager.server.model.HasUUID;
import de.iks.grocery_manager.server.security.UserInfo;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public abstract class OwnerTrackingCRUDController<Entity extends HasUUID & HasOwner, ListDTO extends HasUUID_DTO,
    CreateDTO, UpdateDTO, Repository extends OwnerTrackingJpaRepository<@NonNull Entity>> {
    public static abstract class Standard<Entity extends HasUUID & HasOwner, ListDTO extends HasUUID_DTO, CreateDTO,
        Repository extends OwnerTrackingJpaRepository<@NonNull Entity>>
        extends OwnerTrackingCRUDController<Entity, ListDTO, CreateDTO, CreateDTO, Repository> {
        public Standard(
            Repository repository,
            Owned<Entity, ListDTO, CreateDTO, CreateDTO> dtoMapper
        ) {
            super(repository, dtoMapper);
        }
    }

    protected final Repository repository;
    private final EntityMapper.Owned<Entity, ListDTO, CreateDTO, UpdateDTO> dtoMapper;
    @Inject
    protected UserInfo userInfo;
    @Inject
    protected UriInfo uriInfo;

    @GET
    @Path("{uuid}")
    public RestResponse<ListDTO> get(
        @PathParam("uuid") UUID uuid
    ) {
        return repository
            .findByUuidAndOwner(uuid, userInfo.getOwner())
            .map(dtoMapper.map())
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @PUT
    @Path("{uuid}")
    public RestResponse<ListDTO> update(
        @PathParam("uuid") UUID uuid,
        UpdateDTO createProductGroupDTO
    ) {
        return repository
            .findByUuidAndOwner(uuid, userInfo.getOwner())
            .map(p -> {
                dtoMapper
                    .update()
                    .accept(
                        p,
                        createProductGroupDTO
                    );
                return p;
            })
            .map(repository::saveAndFlush)
            .map(dtoMapper.map())
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @DELETE
    @Path("{uuid}")
    @ResponseStatus(200)
    public void delete(@PathParam("uuid") UUID uuid) {
        repository.deleteByUuidAndOwner(uuid, userInfo.getOwner());
    }

    @POST
    @ResponseStatus(201)
    public RestResponse<ListDTO> create(
        CreateDTO createProductGroupDTO
    ) {
        ListDTO ret = dtoMapper
            .map()
            .apply(
                repository.saveAndFlush(
                    dtoMapper
                        .create()
                        .apply(
                            createProductGroupDTO,
                            userInfo.getOwner()
                        )
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
}
