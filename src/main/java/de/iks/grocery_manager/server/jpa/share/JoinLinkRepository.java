package de.iks.grocery_manager.server.jpa.share;

import de.iks.grocery_manager.server.model.share.JoinLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JoinLinkRepository extends JpaRepository<JoinLink, UUID> {
}
