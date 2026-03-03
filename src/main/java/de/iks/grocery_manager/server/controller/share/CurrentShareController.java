package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.config.ShareFilter.SharePrincipal;
import de.iks.grocery_manager.server.dto.DTOMapper;
import de.iks.grocery_manager.server.dto.share.CreateShareDTO;
import de.iks.grocery_manager.server.dto.share.ShareDTO;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.model.share.Share;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import static de.iks.grocery_manager.server.util.OwnerUtils.getOwner;
import static de.iks.grocery_manager.server.util.OwnerUtils.getUser;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/share/current")
@Transactional
public class CurrentShareController {
    private final CleanupService cleanupService;
    private final ShareRepository shares;
    private final DTOMapper dtoMapper;

    @GetMapping
    @Transactional(readOnly = true)
    public ShareDTO getCurrent(@AuthenticationPrincipal SharePrincipal principal) {
        return dtoMapper.map(principal.share(), getUser(principal.principal()));
    }

    @PutMapping
    public ShareDTO updateCurrent(
        @RequestBody CreateShareDTO dto,
        @AuthenticationPrincipal SharePrincipal principal
    ) {
        final Share share = principal.share();
        dtoMapper.update(share, dto);
        return dtoMapper.map(shares.saveAndFlush(share), getUser(principal.principal()));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteCurrent(@AuthenticationPrincipal SharePrincipal principal) {
        cleanupService.deleteOwner(getOwner(principal));
        shares.delete(principal.share());
        return ResponseEntity.ok().build();
    }
}
