package de.iks.grocery_manager.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.jpa.OwnerTrackingJpaRepository;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.HasOwner;
import de.iks.grocery_manager.server.model.HasUUID;
import de.iks.grocery_manager.server.security.UserInfo;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;
import org.jspecify.annotations.NonNull;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public abstract class OwnerTrackingCRUDController<Entity extends HasUUID & HasOwner, DTO extends HasUUID_DTO,
    Repository extends OwnerTrackingJpaRepository<@NonNull Entity>> {

    protected final Repository repository;
    private final EntityMapper.Owned<Entity, DTO> dtoMapper;
    @Inject
    protected UserInfo userInfo;
    @Inject
    protected UriInfo uriInfo;

    protected final CacheControl cacheControl = new CacheControl();

    {
        cacheControl.setMaxAge(60);
        cacheControl.setPrivate(true);
    }

    @GET
    @Path("{uuid}")
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> get(
        @PathParam("uuid") UUID uuid
    ) {
        return repository
            .findByUuidAndOwner(uuid, userInfo.getOwner())
            .map(dtoMapper.map())
            .map(dto -> RestResponse.ResponseBuilder
                .ok(dto)
                .cacheControl(cacheControl)
                .tag(new EntityTag(Integer.toString(dto.version())))
                .build()
            )
            .orElseGet(RestResponse::notFound);
    }

    @PUT
    @Path("{uuid}")
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> update(
        @PathParam("uuid") UUID uuid,
        @JsonView(DTOViews.Update.class) DTO updateDto
    ) {
        return repository
            .findByUuidAndOwner(uuid, userInfo.getOwner())
            .map(p -> {
                dtoMapper
                    .update()
                    .accept(
                        p,
                        updateDto
                    );
                return p;
            })
            .map(repository::saveAndFlush)
            .map(dtoMapper.map())
            .map(dto -> RestResponse.ResponseBuilder
                .ok(dto)
                .tag(new EntityTag(Integer.toString(dto.version())))
                .contentLocation(uriInfo.getRequestUri())
                .cacheControl(cacheControl)
                .build()
            )
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
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> create(
        @JsonView(DTOViews.Create.class) DTO createProductGroupDTO
    ) {
        DTO ret = dtoMapper
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
        URI location = uriInfo
            .getAbsolutePathBuilder()
            .path(ret.uuid().toString())
            .build();
        return RestResponse.ResponseBuilder
            .create(Status.CREATED, ret)
            .location(location)
            .contentLocation(location)
            .tag(new EntityTag(Integer.toString(ret.version())))
            .cacheControl(cacheControl)
            .build();
    }
}
