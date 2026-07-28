package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.dto.share.CreateJoinLinkDTO;
import de.iks.grocery_manager.server.dto.share.JoinLinkDTO;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.security.UserInfo;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Path("/api/share/current/links")
@Transactional
public class JoinLinkController {
    private final JoinLinkRepository links;
    private final DTOMapper dtoMapper;
    private final UserInfo userInfo;
    private final UriInfo uriInfo;

    private static Predicate<? super JoinLink> sameShare(UUID shareId) {
        return l -> l
            .getShare()
            .getUuid()
            .equals(shareId);
    }

    @GET
    public List<JoinLinkDTO> get() {
        return dtoMapper.map(links.findAllByShare(userInfo.getShareId()));
    }

    @GET
    @Path("{uuid}")
    public RestResponse<JoinLinkDTO> get(
        @PathParam("uuid") UUID uuid
    ) {
        return links
            .findByIdOptional(uuid)
            .filter(sameShare(userInfo.getShareId()))
            .map(dtoMapper::map)
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @POST
    public RestResponse<JoinLinkDTO> create(
        CreateJoinLinkDTO dto
    ) {
        JoinLinkDTO created = dtoMapper.map(links.saveAndFlush(dtoMapper.create(dto, userInfo.getShareId())));
        return RestResponse.ResponseBuilder
            .create(Status.CREATED, created)
            .location(
                uriInfo
                    .getAbsolutePathBuilder()
                    .path(created.uuid().toString())
                    .build()
            )
            .build();
    }

    @PUT
    @Path("{uuid}")
    public RestResponse<JoinLinkDTO> create(
        @PathParam("uuid") UUID uuid,
        CreateJoinLinkDTO dto
    ) {
        return links
            .findByIdOptional(uuid)
            .filter(sameShare(userInfo.getShareId()))
            .map(l -> {
                dtoMapper.update(l, dto);
                return l;
            })
            .map(links::saveAndFlush)
            .map(dtoMapper::map)
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @DELETE
    @Path("{uuid}")
    public RestResponse<?> delete(
        @PathParam("uuid") UUID uuid
    ) {
        List<JoinLink> links = this.links.findAllByShare(userInfo.getShareId());
        if(links
            .stream()
            .filter(j -> !j
                .getUuid()
                .equals(uuid))
            .map(JoinLink::getPermissions)
            .noneMatch(Permissions.ADMIN::equals)) {
            return RestResponse.status(Status.BAD_REQUEST);
        }
        if(!links.removeIf(l -> l
            .getUuid()
            .equals(uuid))) {
            return RestResponse.notFound();
        }
        this.links.deleteById(uuid);
        return RestResponse.ok();
    }
}
