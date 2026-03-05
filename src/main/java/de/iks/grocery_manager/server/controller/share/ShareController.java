package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.share.CreateShareDTO;
import de.iks.grocery_manager.server.dto.share.ShareDTO;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Set;
import java.util.UUID;

import static de.iks.grocery_manager.server.util.OwnerUtils.getUser;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/share")
@Transactional
public class ShareController {
    private final ShareRepository shares;
    private final JoinLinkRepository links;
    private final DTOMapper dtoMapper;

    @PostMapping
    public ResponseEntity<ShareDTO> create(
        @RequestBody CreateShareDTO createShareDTO,
        @AuthenticationPrincipal Object principal,
        UriComponentsBuilder uriBuilder
    ) {
        final String user = getUser(principal);
        Share ret = dtoMapper.create(createShareDTO);
        JoinLink ownerLink = new JoinLink();
        ownerLink.setShare(ret);
        ownerLink.setName("Owner");
        ownerLink.setUsers(Set.of(user));
        ownerLink.setPermissions(Permissions.ADMIN);
        ret.getLinks().add(ownerLink);
        ret = shares.save(ret);
        return ResponseEntity
            .created(
                uriBuilder
                    .pathSegment("api", "share", "current")
                    .queryParam("share", ret.getUuid())
                    .build()
                    .toUri()
            )
            .body(dtoMapper.map(ret, user));
    }

    @PostMapping("/join/{uuid}")
    public ResponseEntity<ShareDTO> join(
        @PathVariable UUID uuid,
        @AuthenticationPrincipal Object principal
    ) {
        final String user = getUser(principal);
        return ResponseEntity.of(
            links
                .findById(uuid)
                .map(j -> j.use(user))
                .map(links::saveAndFlush)
                .map(JoinLink::getShare)
                .map(s -> dtoMapper.map(s, user))
        );
    }

    @GetMapping
    @Transactional(readOnly = true)
    public Page<ShareDTO> getAll(
        @PageableDefault Pageable pageable,
        @AuthenticationPrincipal Object principal
    ) {
        final String user = getUser(principal);
        return shares
            .findByUser(user, pageable)
            .map(s -> dtoMapper.map(s, user));
    }
}
