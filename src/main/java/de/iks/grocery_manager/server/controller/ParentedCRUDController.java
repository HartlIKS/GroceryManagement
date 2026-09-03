package de.iks.grocery_manager.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.jpa.share.ParentTrackingRepository;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.mapping.EntityMapper;
import de.iks.grocery_manager.server.mapping.HasUUID_DTO;
import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.ResponseStatus;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@Transactional
public abstract class ParentedCRUDController<Entity extends HasUUID, DTO extends HasUUID_DTO,
    Repository extends ParentTrackingRepository<Entity> & BaseRepository<Entity>> {

    protected final Repository repository;
    private final EntityMapper.Parented<Entity, DTO> dtoMapper;
    @Inject
    protected UriInfo uriInfo;
    protected final CacheControl cacheControl = new CacheControl();

    {
        cacheControl.setMaxAge(600);
    }


    @GET
    @Path("{uuid}")
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> get(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("parentUuid") UUID parentUuid,
        @PathParam("uuid") UUID uuid
    ) {
        return repository
            .findByIdOptional(parentUuid, uuid)
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
    public RestResponse<DTO> put(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("parentUuid") UUID parentUuid,
        @PathParam("uuid") UUID uuid,
        @JsonView(DTOViews.Update.class) DTO updateDTO
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
            .map(dto -> RestResponse.ResponseBuilder
                .ok(dto)
                .cacheControl(cacheControl)
                .tag(new EntityTag(Integer.toString(dto.version())))
                .contentLocation(uriInfo.getRequestUri())
                .build()
            )
            .orElseGet(RestResponse::notFound);
    }

    @POST
    @ResponseStatus(201)
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> create(
        @PathParam("parentUuid") UUID parentUuid,
        @JsonView(DTOViews.Create.class) DTO createDTO
    ) {
        DTO ret = dtoMapper
            .map()
            .apply(repository.saveAndFlush(dtoMapper
                                               .create()
                                               .apply(createDTO, parentUuid)));
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
