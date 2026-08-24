package de.iks.grocery_manager.server.controller;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.extra_http.QUERY;
import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.mapping.DTOViews;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Transactional
public abstract class CRUDController<Entity extends HasUUID, DTO extends HasUUID_DTO,
    Repository extends BaseRepository<Entity>> {

    protected final Repository repository;
    private final EntityMapper<Entity, DTO> dtoMapper;
    @Inject
    protected UriInfo uriInfo;

    @GET
    @Path("{uuid}")
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> get(@PathParam("uuid") UUID uuid) {
        return repository
            .findByIdOptional(uuid)
            .map(dtoMapper.map())
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @QUERY
    @JsonView(DTOViews.List.class)
    public Map<UUID, DTO> getMany(List<UUID> uuids) {
        return repository
            .streamByIds(uuids)
            .map(dtoMapper.map())
            .collect(Collectors.toUnmodifiableMap(DTO::uuid, Function.identity()));
    }

    @PUT
    @Path("{uuid}")
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> put(
        @PathParam("uuid") UUID uuid,
        @JsonView(DTOViews.Update.class) DTO updateDTO
    ) {
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
    @JsonView(DTOViews.List.class)
    public RestResponse<DTO> create(@JsonView(DTOViews.Create.class) DTO createDTO) {
        DTO ret = dtoMapper
            .map()
            .apply(
                repository.saveAndFlush(
                    dtoMapper
                        .create()
                        .apply(createDTO)
                )
            );
        return RestResponse.ResponseBuilder
            .create(Status.CREATED, ret)
            .location(
                uriInfo
                    .getAbsolutePathBuilder()
                    .path(ret
                              .uuid()
                              .toString())
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
