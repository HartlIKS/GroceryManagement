package de.iks.grocery_manager.server.controller.share;

import com.fasterxml.jackson.annotation.JsonView;
import de.iks.grocery_manager.server.dto.share.ShareDTO;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.mapping.DTOMapper;
import de.iks.grocery_manager.server.mapping.DTOViews;
import de.iks.grocery_manager.server.model.share.Share;
import de.iks.grocery_manager.server.security.UserInfo;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.jboss.resteasy.reactive.ResponseStatus;

@RequiredArgsConstructor
@Path("/api/share/current")
@Transactional
public class CurrentShareController {
    private final CleanupService cleanupService;
    private final ShareRepository shares;
    private final DTOMapper dtoMapper;
    private final UserInfo userInfo;

    @GET
    @JsonView(DTOViews.List.class)
    public ShareDTO getCurrent() {
        return dtoMapper.map(shares.findByIdOptional(userInfo.getShareId()).orElseThrow(), userInfo.getUser());
    }

    @PUT
    @JsonView(DTOViews.List.class)
    public ShareDTO updateCurrent(
        @JsonView(DTOViews.Update.class) ShareDTO dto
    ) {
        final Share share = shares.findByIdOptional(userInfo.getShareId()).orElseThrow();
        dtoMapper.update(share, dto);
        return dtoMapper.map(shares.saveAndFlush(share), userInfo.getUser());
    }

    @DELETE
    @ResponseStatus(200)
    public void deleteCurrent() {
        cleanupService.deleteOwner(userInfo.getOwner());
        shares.deleteById(userInfo.getShareId());
    }
}
