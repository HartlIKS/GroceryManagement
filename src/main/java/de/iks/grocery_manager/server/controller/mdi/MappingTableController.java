package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.dto.JsonString;
import de.iks.grocery_manager.server.extra_http.QUERY;
import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.MappingHandler;
import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.ws.rs.*;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;
import org.jspecify.annotations.NonNull;
import jakarta.transaction.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Transactional
public abstract class MappingTableController<
    E extends HasUUID,
    P extends BaseRepository<E>,
    E2 extends HasUUID,
    P2 extends BaseRepository<E2>
    > {
    protected final P repository;
    private final MappingHandler<E, E2> mappingHandler;
    protected final P2 mappedRepository;
    protected final DTOMapper dtoMapper;

    @GET
    public RestResponse<Map<UUID, String>> getMappings(
        @PathParam("uuid") UUID uuid
    ) {
        return repository
            .findByIdOptional(uuid)
            .map(mappingHandler.getMappings())
            .map(dtoMapper::toUUIDMap)
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @QUERY
    @Path("in")
    public RestResponse<Map<String, UUID>> massTranslateInbound(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("uuid") UUID uuid,
        List<String> remoteIds
    ) {
        Map<String, UUID> ret = mappingHandler
            .massTranslateInbound()
            .apply(uuid, remoteIds);
        if(ret.isEmpty() && !repository.existsById(uuid)) {
            return RestResponse.notFound();
        }
        return RestResponse.ok(ret);
    }

    @GET
    @Path("in/{remoteId}")
    public RestResponse<UUID> translateInbound(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("uuid") UUID uuid,
        @PathParam("remoteId") String remoteId
    ) {
        return mappingHandler
            .translateInbound()
            .apply(uuid, remoteId)
            .map(RestResponse::ok)
            .orElseGet(() -> repository.existsById(uuid) ?
                RestResponse
                    .noContent() :
                RestResponse
                    .notFound()
            );
    }

    @PUT
    @Path("in/{remoteId}")
    public RestResponse<UUID> setOutboundTranslation(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("uuid") UUID uuid,
        @PathParam("remoteId") String remoteId,
        UUID localId
    ) {
        return mappedRepository
            .findByIdOptional(localId)
            .flatMap(e -> {
                Optional<@NonNull E> t = repository.findByIdOptional(uuid);
                t
                    .map(mappingHandler.getMappings())
                    .ifPresent(m -> m.put(e, remoteId));
                return t
                    .map(repository::saveAndFlush)
                    .map(mappingHandler.getMappings())
                    .filter(m -> m.containsKey(e))
                    .map(ignored -> localId);
            })
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @QUERY
    @Path("out")
    public RestResponse<Map<UUID, String>> massTranslateOutbound(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("uuid") UUID uuid,
        List<UUID> localIds
    ) {
        Map<UUID, String> ret = mappingHandler
            .massTranslateOutbound()
            .apply(uuid, localIds);
        if(ret.isEmpty() && !repository.existsById(uuid)) {
            return RestResponse.notFound();
        }
        return RestResponse.ok(ret);
    }
    @GET
    @Path("out/{localId}")
    public RestResponse<JsonString> translateOutbound(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("uuid") UUID uuid,
        @PathParam("localId") UUID localId
    ) {
        return mappingHandler
            .translateOutbound()
            .apply(uuid, localId)
            .map(JsonString::new)
            .map(RestResponse::ok)
            .orElseGet(() -> repository.existsById(uuid) ?
                RestResponse
                    .noContent() :
                RestResponse
                    .notFound()
            );
    }

    @PUT
    @Path("out/{localId}")
    public RestResponse<JsonString> setOutboundTranslation(
        @SuppressWarnings("UnresolvedRestParam") @PathParam("uuid") UUID uuid,
        @PathParam("localId") UUID localId,
        JsonString remoteId
    ) {
        return mappedRepository
            .findByIdOptional(localId)
            .flatMap(e -> {
                Optional<@NonNull E> t = repository.findByIdOptional(uuid);
                t
                    .map(mappingHandler.getMappings())
                    .ifPresent(m -> m.put(e, remoteId.str()));
                return t
                    .map(repository::saveAndFlush)
                    .map(mappingHandler.getMappings())
                    .map(m -> m.get(e));
            })
            .map(JsonString::new)
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }
}
