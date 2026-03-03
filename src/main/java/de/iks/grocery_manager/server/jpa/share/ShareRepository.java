package de.iks.grocery_manager.server.jpa.share;

import de.iks.grocery_manager.server.model.share.Share;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface ShareRepository extends JpaRepository<Share, UUID> {
    @Query("""
select s
from Share s
where exists(
    select l
    from s.links l
    join l.users u on u = :user
    where l.permissions > 0
)
""")
    Page<? extends Share> findByUser(String user, Pageable pageable);
}
