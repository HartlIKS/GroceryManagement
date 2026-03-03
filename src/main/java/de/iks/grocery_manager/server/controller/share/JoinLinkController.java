package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.config.ShareFilter.SharePrincipal;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.share.CreateJoinLinkDTO;
import de.iks.grocery_manager.server.dto.share.JoinLinkDTO;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/share/current/links")
@Transactional
public class JoinLinkController {
    private final JoinLinkRepository links;
    private final DTOMapper dtoMapper;

    private static Predicate<? super JoinLink> sameShare(Share share) {
        return l -> l.getShare().getUuid().equals(share.getUuid());
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<JoinLinkDTO> get(@AuthenticationPrincipal SharePrincipal principal) {
        return dtoMapper.map(principal
                                 .share()
                                 .getLinks());
    }

    @GetMapping("/{uuid}")
    @Transactional(readOnly = true)
    public ResponseEntity<JoinLinkDTO> get(@PathVariable UUID uuid, @AuthenticationPrincipal SharePrincipal principal) {
        return ResponseEntity.of(
            links
                .findById(uuid)
                .filter(sameShare(principal.share()))
                .map(dtoMapper::map)
        );
    }

    @PostMapping
    public ResponseEntity<JoinLinkDTO> create(
        @RequestBody CreateJoinLinkDTO dto,
        @AuthenticationPrincipal SharePrincipal principal,
        UriComponentsBuilder uriBuilder
    ) {
        JoinLinkDTO created = dtoMapper.map(links.saveAndFlush(dtoMapper.create(dto, principal.share())));
        return ResponseEntity
            .created(
                uriBuilder
                    .pathSegment("api", "share", "current", "links", "{uuid}")
                    .build(created.uuid())
            )
            .body(created);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<JoinLinkDTO> create(
        @PathVariable UUID uuid,
        @RequestBody CreateJoinLinkDTO dto,
        @AuthenticationPrincipal SharePrincipal principal
    ) {
        return ResponseEntity.of(
            links
                .findById(uuid)
                .filter(sameShare(principal.share()))
                .map(l -> {
                    dtoMapper.update(l, dto);
                    return l;
                })
                .map(links::saveAndFlush)
                .map(dtoMapper::map)
        );
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(
        @PathVariable UUID uuid,
        @AuthenticationPrincipal SharePrincipal principal
    ) {
        List<JoinLink> links = principal
            .share()
            .getLinks();
        if(!links.removeIf(l -> l.getUuid().equals(uuid))) return ResponseEntity.notFound().build();
        if(links.stream().map(JoinLink::getPermissions).noneMatch(Permissions.ADMIN::equals)) {
            throw new IllegalArgumentException("Cannot delete last admin link of a share");
        }
        return ResponseEntity.ok().build();
    }
}
