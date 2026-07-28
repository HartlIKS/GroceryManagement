package de.iks.grocery_manager.server.jpa.share;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import io.quarkus.hibernate.orm.panache.common.ProjectedFieldName;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class ShareRepository implements BaseRepository<Share> {
    public Stream<? extends Share> findByUser(String user) {
        return stream(
            """
            select s
            from Share s
            where exists(
                select l
                from s.links l
                join l.users u on u = ?1
                where l.permissions > 0
            )""",
            user
        );
    }

    @RegisterForReflection
    private record MaxPerms(
        @ProjectedFieldName("max(l.permissions)") Permissions permissions
    ) {
    }

    public Permissions getPermissionsForUser(UUID share, String user) {
        return find(
            """
            from Share s
            join s.links l
            join l.users u on u = :user
            where s.uuid = :share
            """,
            Map.of("share", share, "user", user)
        ).project(MaxPerms.class)
            .firstResultOptional()
            .map(MaxPerms::permissions)
            .orElse(Permissions.NONE);
    }
}
