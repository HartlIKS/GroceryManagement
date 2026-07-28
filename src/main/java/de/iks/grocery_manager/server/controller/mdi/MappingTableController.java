package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.dto.JsonString;
import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.MappingHandler;
import de.iks.grocery_manager.server.model.HasUUID;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;
import org.jspecify.annotations.NonNull;
import jakarta.transaction.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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
