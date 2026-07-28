package de.iks.grocery_manager.server.jpa.share;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.share.JoinLink;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class JoinLinkRepository implements BaseRepository<JoinLink> {
    public List<JoinLink> findAllByShare(UUID shareId) {
        return list("share.uuid", shareId);
    }
}
