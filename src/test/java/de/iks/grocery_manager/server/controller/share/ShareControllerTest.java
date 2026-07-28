package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(ShareController.class)
@WithTestUser
@Sql("/testdata.sql")
class ShareControllerTest {

    @Inject
    ShareRepository shareRepository;

    @Inject
    JoinLinkRepository joinLinkRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(ShareController.class)
    @WithTestUser
    class CreateShare {
        @Test
        void shouldCreateShareWhenAuthorized() {
            long initialShareCount = shareRepository.count();
            long initialLinkCount = joinLinkRepository.count();

            String createJson = """
                {
                  "name": "Test Share"
                }""";

            UUID uuid = expect()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .header(
                    "location",
                    matchesRegex(String.format("%s/current\\?share=%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern()))
                )
                .body("name", is("Test Share"))
                .body("permissions", is("ADMIN"))
                .given()
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post()
                .jsonPath()
                .getUUID("uuid");


            Share createdShare = shareRepository
                .findByIdOptional(uuid)
                .orElse(null);

            assertNotNull(createdShare);
            // Verify the share and owner link were created correctly
            assertEquals("Test Share", createdShare.getName());
            assertEquals(
                1,
                createdShare
                    .getLinks()
                    .size()
            );
            JoinLink ownerLink = createdShare
                .getLinks()
                .get(0);
            assertEquals(createdShare, ownerLink.getShare());
            assertEquals(Permissions.ADMIN, ownerLink.getPermissions());
            assertTrue(ownerLink
                           .getUsers()
                           .contains(WithTestUser.OWNER));
            assertFalse(ownerLink.isActive()); // Owner links are inactive by default

            // Verify creation
            assertEquals(initialShareCount + 1, shareRepository.count());
            assertEquals(initialLinkCount + 1, joinLinkRepository.count());
        }

        @Test
        void shouldCreateShareWithDifferentName() {
            String createJson = """
                {
                  "name": "Family Shopping List"
                }""";

            UUID uuid = expect()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .header(
                    "location",
                    matchesRegex(String.format("%s/current\\?share=%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern()))
                )
                .body("name", is("Family Shopping List"))
                .body("permissions", is("ADMIN"))
                .given()
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post()
                .jsonPath()
                .getUUID("uuid");

            Share createdShare = shareRepository
                .findByIdOptional(uuid)
                .orElse(null);
            assertNotNull(createdShare);
            assertEquals("Family Shopping List", createdShare.getName());
            assertEquals(
                1,
                createdShare
                    .getLinks()
                    .size()
            );
        }
    }

    @Nested
    @TestHTTPEndpoint(ShareController.class)
    @WithTestUser
    class JoinShare {
        @Test
        void shouldJoinShareWhenLinkExistsAndActive() {
            QuarkusTransaction.begin();

            // Create a share and join link first
            Share testShare = new Share();
            testShare.setName("Test Share");
            testShare.setLinks(new ArrayList<>());
            shareRepository.persist(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Test Link");
            joinLink.setPermissions(Permissions.WRITE);
            joinLink.setActive(true);
            joinLink.setSingleUse(false);
            joinLink.setUsers(new HashSet<>(Set.of(WithTestUser2.OWNER))); // Another user already joined
            joinLinkRepository.persist(joinLink);

            // Add the link to the share's links list
            testShare
                .getLinks()
                .add(joinLink);
            shareRepository.persist(testShare);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", is("Test Share"))
                .body("permissions", is("WRITE"))
                .body(
                    "uuid",
                    is(testShare
                           .getUuid()
                           .toString())
                )
                .when()
                .post("join/{uuid}", joinLink.getUuid());

            // Verify user was added to the join link
            JoinLink updatedLink = joinLinkRepository
                .findByIdOptional(joinLink.getUuid())
                .orElseThrow();
            assertTrue(updatedLink
                           .getUsers()
                           .contains(WithTestUser.OWNER));
            assertTrue(updatedLink
                           .getUsers()
                           .contains(WithTestUser2.OWNER));
        }

        @Test
        void shouldJoinShareWithSingleUseLinkAndDeactivateIt() {
            QuarkusTransaction.begin();

            // Create a share and single-use join link
            Share testShare = new Share();
            testShare.setName("Single Use Share");
            testShare.setLinks(new ArrayList<>());
            shareRepository.persist(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Single Use Link");
            joinLink.setPermissions(Permissions.READ);
            joinLink.setActive(true);
            joinLink.setSingleUse(true);
            joinLink.setUsers(new HashSet<>());
            joinLinkRepository.persist(joinLink);

            // Add the link to the share's links list
            testShare
                .getLinks()
                .add(joinLink);
            shareRepository.persist(testShare);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", is("Single Use Share"))
                .body("permissions", is("READ"))
                .body(
                    "uuid",
                    is(testShare
                           .getUuid()
                           .toString())
                )
                .when()
                .post("join/{uuid}", joinLink.getUuid());

            // Verify link was deactivated after use
            JoinLink updatedLink = joinLinkRepository
                .findByIdOptional(joinLink.getUuid())
                .orElseThrow();
            assertFalse(updatedLink.isActive());
            assertTrue(updatedLink
                           .getUsers()
                           .contains(WithTestUser.OWNER));
        }

        @Test
        void shouldReturn404WhenJoinLinkNotFound() {
            expect()
                .statusCode(404)
                .when()
                .post("join/{uuid}", Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn404WhenJoinLinkInactive() {
            QuarkusTransaction.begin();

            // Create a share and inactive join link
            Share testShare = new Share();
            testShare.setName("Inactive Share");
            testShare.setLinks(new ArrayList<>());
            shareRepository.persist(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Inactive Link");
            joinLink.setPermissions(Permissions.READ);
            joinLink.setActive(false); // Inactive link
            joinLink.setSingleUse(false);
            joinLink.setUsers(new HashSet<>());
            joinLinkRepository.persist(joinLink);

            // Add the link to the share's links list
            testShare
                .getLinks()
                .add(joinLink);
            shareRepository.persist(testShare);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .when()
                .post("join/{uuid}", joinLink.getUuid());
        }

        @Test
        void shouldReturn404WhenJoinLinkExpired() {
            QuarkusTransaction.begin();

            // Create a share and expired join link
            Share testShare = new Share();
            testShare.setName("Expired Share");
            testShare.setLinks(new ArrayList<>());
            shareRepository.persist(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Expired Link");
            joinLink.setPermissions(Permissions.READ);
            joinLink.setActive(true);
            joinLink.setSingleUse(false);
            joinLink.setValidTo(java.time.Instant
                                    .now()
                                    .minusSeconds(3600)); // Expired 1 hour ago
            joinLink.setUsers(new HashSet<>());
            joinLinkRepository.persist(joinLink);

            // Add the link to the share's links list
            testShare
                .getLinks()
                .add(joinLink);
            shareRepository.persist(testShare);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .when()
                .post("join/{uuid}", joinLink.getUuid());
        }
    }

    @Nested
    @TestHTTPEndpoint(ShareController.class)
    @WithTestUser
    class GetAllShares {
        @Test
        void shouldReturnUserSharesOnly() {
            QuarkusTransaction.begin();

            // Create shares for current user
            Share userShare1 = createShareWithOwner("User Share 1", WithTestUser.OWNER, Permissions.ADMIN);
            Share userShare2 = createShareWithOwner("User Share 2", WithTestUser.OWNER, Permissions.WRITE);

            // Create shares for different user
            createShareWithOwner("Other Share 1", WithTestUser2.OWNER, Permissions.ADMIN);
            createShareWithOwner("Other Share 2", WithTestUser2.OWNER, Permissions.READ);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("find { it.uuid == '%s' }.permissions", withArgs(userShare1.getUuid()), is("ADMIN"))
                .body("find { it.uuid == '%s' }.permissions", withArgs(userShare2.getUuid()), is("WRITE"))
                .when()
                .get();
        }

        @Test
        void shouldReturnEmptyWhenUserHasNoShares() {
            QuarkusTransaction.begin();
            // Create shares for different user only
            createShareWithOwner("Other Share 1", WithTestUser2.OWNER, Permissions.ADMIN);
            createShareWithOwner("Other Share 2", WithTestUser2.OWNER, Permissions.READ);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(0))
                .when()
                .get();
        }

        @Test
        void shouldReturnCorrectPermissionsForUser() {
            QuarkusTransaction.begin();

            // Create shares with different permission levels for current user
            Share adminShare = createShareWithOwner("Admin Share", WithTestUser.OWNER, Permissions.ADMIN);
            Share writeShare = createShareWithOwner("Write Share", WithTestUser.OWNER, Permissions.WRITE);
            Share readShare = createShareWithOwner("Read Share", WithTestUser.OWNER, Permissions.READ);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(3))
                .body("find { it.uuid == '%s' }.permissions", withArgs(adminShare.getUuid()), is("ADMIN"))
                .body("find { it.uuid == '%s' }.permissions", withArgs(writeShare.getUuid()), is("WRITE"))
                .body("find { it.uuid == '%s' }.permissions", withArgs(readShare.getUuid()), is("READ"))
                .when()
                .get();
        }
    }

    // Helper method to create a share with an owner join link
    private Share createShareWithOwner(String name, String user, Permissions permissions) {
        Share share = new Share();
        share.setName(name);
        share.setLinks(new ArrayList<>()); // Initialize the links list
        shareRepository.persist(share);

        JoinLink ownerLink = new JoinLink();
        ownerLink.setShare(share);
        ownerLink.setName("Owner Link for " + name);
        ownerLink.setUsers(new HashSet<>(Set.of(user)));
        ownerLink.setPermissions(permissions);
        ownerLink.setActive(true);
        ownerLink.setSingleUse(false);
        joinLinkRepository.persist(ownerLink);

        // Add the link to the share's links list
        share
            .getLinks()
            .add(ownerLink);
        shareRepository.persist(share);

        return share;
    }
}
