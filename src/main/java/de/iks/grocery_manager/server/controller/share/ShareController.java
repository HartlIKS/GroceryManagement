package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.dto.share.CreateShareDTO;
import de.iks.grocery_manager.server.dto.share.ShareDTO;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import de.iks.grocery_manager.server.security.UserInfo;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.UriInfo;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.Status;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Path("/api/share")
@Transactional
public class ShareController {
    private final ShareRepository shares;
    private final JoinLinkRepository links;
    private final DTOMapper dtoMapper;
    private final UserInfo userInfo;
    private final UriInfo uriInfo;

    @POST
    public RestResponse<ShareDTO> create(
        CreateShareDTO createShareDTO
    ) {
        Share ret = dtoMapper.create(createShareDTO);
        JoinLink ownerLink = new JoinLink();
        ownerLink.setShare(ret);
        ownerLink.setName("Owner");
        ownerLink.setUsers(Set.of(userInfo.getUser()));
        ownerLink.setPermissions(Permissions.ADMIN);
        ret
            .getLinks()
            .add(ownerLink);
        ret = shares.saveAndFlush(ret);
        return RestResponse.ResponseBuilder
            .create(Status.CREATED, dtoMapper.map(ret, userInfo.getUser()))
            .location(
                uriInfo
                    .getAbsolutePathBuilder()
                    .path("current")
                    .queryParam("share", ret.getUuid())
                    .build()
            )
            .build();
    }

    @POST
    @Path("join/{uuid}")
    public RestResponse<ShareDTO> join(
        @PathParam("uuid") UUID uuid
    ) {
        return links
            .findByIdOptional(uuid)
            .map(j -> j.use(userInfo.getUser()))
            .map(links::saveAndFlush)
            .map(JoinLink::getShare)
            .map(s -> dtoMapper.map(s, userInfo.getUser()))
            .map(RestResponse::ok)
            .orElseGet(RestResponse::notFound);
    }

    @GET
    public List<ShareDTO> getAll() {
        return shares
            .findByUser(userInfo.getUser())
            .map(s -> dtoMapper.map(s, userInfo.getUser()))
            .toList();
    }
}
