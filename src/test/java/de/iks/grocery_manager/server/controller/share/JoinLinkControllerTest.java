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

import java.util.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(JoinLinkController.class)
@WithTestUser
@Sql("/testdata.sql")
class JoinLinkControllerTest {

    @Inject
    ShareRepository shareRepository;

    @Inject
    JoinLinkRepository joinLinkRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(JoinLinkController.class)
    @WithTestUser
    class GetAllLinks {
        @Test
        void shouldReturnAllLinksForUserWithAdminPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having ADMIN permissions and multiple links
            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 3);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(3))
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get();
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having WRITE permissions
            Share testShare = createShareWithLinks("Write Share", WithTestUser.OWNER, Permissions.WRITE, 2);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get();
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having READ permissions
            Share testShare = createShareWithLinks("Read Share", WithTestUser.OWNER, Permissions.READ, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get();
        }

        @Test
        void shouldReturnEmptyArrayWhenShareHasOnlyAccessLink() {
            QuarkusTransaction.begin();

            // Create a share with only the owner link (no additional links)
            Share testShare = createShareWithLinks("Minimal Share", WithTestUser.OWNER, Permissions.ADMIN, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get();
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() {
            QuarkusTransaction.begin();

            // Create a share where user has no permissions
            Share testShare = createShareWithLinks("No Access Share", WithTestUser2.OWNER, Permissions.ADMIN, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get();
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            expect()
                .statusCode(403)
                .given()
                .queryParam("share", Testdata.BAD_UUID)
                .when()
                .get();
        }

        @Test
        void shouldReturn403WhenNoShareParameterProvided() {
            expect()
                .statusCode(403)
                .when()
                .get();
        }
    }

    @Nested
    @TestHTTPEndpoint(JoinLinkController.class)
    @WithTestUser
    class GetLinkById {
        @Test
        void shouldReturnLinkWhenUserHasAdminPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 2);
            JoinLink expectedLink = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "uuid",
                    is(expectedLink
                           .getUuid()
                           .toString())
                )
                .body("name", is(expectedLink.getName()))
                .body(
                    "permissions",
                    is(expectedLink
                           .getPermissions()
                           .name())
                )
                .body("active", is(expectedLink.isActive()))
                .body("singleUse", is(expectedLink.isSingleUse()))
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get("{uuid}", expectedLink.getUuid());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having WRITE permissions
            Share testShare = createShareWithLinks("Write Share", WithTestUser.OWNER, Permissions.WRITE, 1);
            JoinLink expectedLink = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get("{uuid}", expectedLink.getUuid());
        }

        @Test
        void shouldReturn403WhenUserWithReadPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having READ permissions
            Share testShare = createShareWithLinks("Read Share", WithTestUser.OWNER, Permissions.READ, 1);
            JoinLink expectedLink = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get("{uuid}", expectedLink.getUuid());
        }

        @Test
        void shouldReturn403WhenLinkExistsButUserHasNoPermissions() {
            QuarkusTransaction.begin();

            // Create a share where user has no permissions
            Share testShare = createShareWithLinks("No Access Share", WithTestUser2.OWNER, Permissions.ADMIN, 1);
            JoinLink link = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get("{uuid}", link.getUuid());

        }

        @Test
        void shouldReturn404WhenLinkDoesNotExist() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            expect()
                .statusCode(403)
                .given()
                .queryParam("share", Testdata.BAD_UUID)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(JoinLinkController.class)
    @WithTestUser
    class CreateLink {
        @Test
        void shouldCreateLinkWhenUserHasAdminPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String createJson = """
                {
                  "name": "New Link",
                  "permissions": "WRITE",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            expect()
                .statusCode(201)
                .header("location", matchesRegex(String.format("%s/%s", baseURI, Testdata.UUID_PATTERN.pattern())))
                .contentType(ContentType.JSON)
                .body("uuid", matchesRegex(Testdata.UUID_PATTERN))
                .body("name", is("New Link"))
                .body("permissions", is("WRITE"))
                .body("active", is(true))
                .body("singleUse", is(false))
                .body("validTo", nullValue())
                .body("numUsers", is(0))
                .given()
                .queryParam("share", testShare.getUuid())
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();

            // Verify link was created in database
            List<JoinLink> links = joinLinkRepository.listAll();
            assertEquals(2, links.size()); // Original link + new link

            JoinLink newLink = links
                .stream()
                .filter(l -> l
                    .getName()
                    .equals("New Link"))
                .findFirst()
                .orElseThrow();
            assertEquals(
                "WRITE",
                newLink
                    .getPermissions()
                    .toString()
            );
            assertTrue(newLink.isActive());
            assertFalse(newLink.isSingleUse());
            assertNull(newLink.getValidTo());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Write Share", WithTestUser.OWNER, Permissions.WRITE, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();

            // Verify no new link was created
            List<JoinLink> links = joinLinkRepository.listAll();
            assertEquals(1, links.size());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Read Share", WithTestUser.OWNER, Permissions.READ, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();

            // Verify no new link was created
            List<JoinLink> links = joinLinkRepository.listAll();
            assertEquals(1, links.size());
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("No Access Share", WithTestUser2.OWNER, Permissions.ADMIN, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", Testdata.BAD_UUID)
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();
        }

        @Test
        void shouldHandleNullFieldsInCreateRequest() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String createJson = """
                {
                  "name": "Minimal Link",
                  "permissions": "READ",
                  "active": null,
                  "singleUse": null,
                  "validTo": null
                }""";

            expect()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("name", is("Minimal Link"))
                .body("permissions", is("READ"))
                .given()
                .queryParam("share", testShare.getUuid())
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();
        }
    }

    @Nested
    @TestHTTPEndpoint(JoinLinkController.class)
    @WithTestUser
    class UpdateLink {
        @Test
        void shouldUpdateLinkWhenUserHasAdminPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 2);
            JoinLink linkToUpdate = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Updated Link",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": "2025-12-31T23:59:59Z"
                }""";

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "uuid",
                    is(linkToUpdate
                           .getUuid()
                           .toString())
                )
                .body("name", is("Updated Link"))
                .body("permissions", is("READ"))
                .body("active", is(false))
                .body("singleUse", is(true))
                .body("validTo", is("2025-12-31T23:59:59Z"))
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", linkToUpdate.getUuid());

            // Verify update in database
            JoinLink updatedLink = joinLinkRepository
                .findByIdOptional(linkToUpdate.getUuid())
                .orElseThrow();
            assertEquals("Updated Link", updatedLink.getName());
            assertEquals(
                "READ",
                updatedLink
                    .getPermissions()
                    .toString()
            );
            assertFalse(updatedLink.isActive());
            assertTrue(updatedLink.isSingleUse());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Write Share", WithTestUser.OWNER, Permissions.WRITE, 1);
            JoinLink linkToUpdate = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "WRITE",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", linkToUpdate.getUuid());

            // Verify link was not updated
            JoinLink unchangedLink = joinLinkRepository
                .findByIdOptional(linkToUpdate.getUuid())
                .orElseThrow();
            assertNotEquals("Should Not Update", unchangedLink.getName());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Read Share", WithTestUser.OWNER, Permissions.READ, 1);
            JoinLink linkToUpdate = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", linkToUpdate.getUuid());

            // Verify link was not updated
            JoinLink unchangedLink = joinLinkRepository
                .findByIdOptional(linkToUpdate.getUuid())
                .orElseThrow();
            assertNotEquals("Should Not Update", unchangedLink.getName());
        }

        @Test
        void shouldReturn403WhenLinkExistsButUserHasNoPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("No Access Share", WithTestUser2.OWNER, Permissions.ADMIN, 1);
            JoinLink linkToUpdate = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", linkToUpdate.getUuid());
        }

        @Test
        void shouldReturn404WhenLinkDoesNotExist() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 1);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            expect()
                .statusCode(404)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", Testdata.BAD_UUID)
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(JoinLinkController.class)
    @WithTestUser
    class DeleteLink {
        @Test
        void shouldDeleteLinkWhenUserHasAdminPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 3);
            // Make sure we have at least 2 admin links
            JoinLink secondAdminLink = testShare
                .getLinks()
                .get(1);
            secondAdminLink.setPermissions(Permissions.ADMIN);
            joinLinkRepository.persist(secondAdminLink);

            JoinLink linkToDelete = testShare
                .getLinks()
                .get(0);
            long initialLinkCount = joinLinkRepository.count();

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", linkToDelete.getUuid());

            // Verify link was deleted
            assertEquals(initialLinkCount - 1, joinLinkRepository.count());
            assertFalse(joinLinkRepository
                            .findByIdOptional(linkToDelete.getUuid())
                            .isPresent());

            // Verify other links still exist
            assertEquals(
                2,
                joinLinkRepository
                    .listAll()
                    .size()
            );
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Write Share", WithTestUser.OWNER, Permissions.WRITE, 2);
            JoinLink linkToDelete = testShare
                .getLinks()
                .get(0);
            long initialLinkCount = joinLinkRepository.count();

            shareRepository.flush();
            joinLinkRepository.flush();
            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", linkToDelete.getUuid());

            // Verify link was not deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
            assertTrue(joinLinkRepository
                           .findByIdOptional(linkToDelete.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Read Share", WithTestUser.OWNER, Permissions.READ, 2);
            JoinLink linkToDelete = testShare
                .getLinks()
                .get(0);
            long initialLinkCount = joinLinkRepository.count();

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", linkToDelete.getUuid());

            // Verify link was not deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
            assertTrue(joinLinkRepository
                           .findByIdOptional(linkToDelete.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn403WhenLinkExistsButUserHasNoPermissions() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("No Access Share", WithTestUser2.OWNER, Permissions.ADMIN, 2);
            JoinLink linkToDelete = testShare
                .getLinks()
                .get(0);
            long initialLinkCount = joinLinkRepository.count();

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", linkToDelete.getUuid());

            // Verify link was not deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
            assertTrue(joinLinkRepository
                           .findByIdOptional(linkToDelete.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn404WhenLinkDoesNotExist() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithLinks("Test Share", WithTestUser.OWNER, Permissions.ADMIN, 2);
            long initialLinkCount = joinLinkRepository.count();

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", Testdata.BAD_UUID);

            // Verify no links were deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            long initialLinkCount = joinLinkRepository.count();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", Testdata.BAD_UUID)
                .when()
                .delete("{uuid}", Testdata.BAD_UUID);

            // Verify no links were deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
        }

        @Test
        void shouldThrowExceptionWhenDeletingLastAdminLink() {
            QuarkusTransaction.begin();

            // Create a share with only one ADMIN link and one WRITE link
            Share testShare = createShareWithLinks("Single Admin Share", WithTestUser.OWNER, Permissions.ADMIN, 2);
            // Make the second link WRITE permissions (not ADMIN)
            JoinLink writeLink = testShare
                .getLinks()
                .get(1);
            writeLink.setPermissions(Permissions.WRITE);
            joinLinkRepository.persist(writeLink);

            // Now we have only one admin link (the first one)
            JoinLink adminLink = testShare
                .getLinks()
                .get(0);

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(400)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", adminLink.getUuid());

            // Verify admin link was not deleted
            assertTrue(joinLinkRepository
                           .findByIdOptional(adminLink.getUuid())
                           .isPresent());
        }

        @Test
        void shouldAllowDeletingNonAdminLinks() {
            QuarkusTransaction.begin();

            // Create a share with one ADMIN and one WRITE link
            Share testShare = createShareWithLinks("Mixed Share", WithTestUser.OWNER, Permissions.ADMIN, 2);
            // Make the second link WRITE permissions
            JoinLink writeLink = testShare
                .getLinks()
                .get(1);
            writeLink.setPermissions(Permissions.WRITE);
            joinLinkRepository.persist(writeLink);

            long initialLinkCount = joinLinkRepository.count();

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", writeLink.getUuid());

            // Verify WRITE link was deleted but ADMIN link remains
            assertEquals(initialLinkCount - 1, joinLinkRepository.count());
            assertFalse(joinLinkRepository
                            .findByIdOptional(writeLink.getUuid())
                            .isPresent());
            assertTrue(joinLinkRepository
                           .findByIdOptional(testShare
                                                 .getLinks()
                                                 .get(0)
                                                 .getUuid())
                           .isPresent());
        }

        @Test
        void shouldAllowDeletingAdminLinkWhenOtherAdminLinkExists() {
            QuarkusTransaction.begin();

            // Create a share with two ADMIN links
            Share testShare = createShareWithLinks("Multi Admin Share", WithTestUser.OWNER, Permissions.ADMIN, 2);
            JoinLink adminLinkToDelete = testShare
                .getLinks()
                .get(0);
            // Ensure both links are ADMIN
            JoinLink secondAdminLink = testShare
                .getLinks()
                .get(1);
            secondAdminLink.setPermissions(Permissions.ADMIN);
            joinLinkRepository.persist(secondAdminLink);

            long initialLinkCount = joinLinkRepository.count();

            shareRepository.flush();
            joinLinkRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .given()
                .queryParam("share", testShare.getUuid())
                .when()
                .delete("{uuid}", adminLinkToDelete.getUuid());

            // Verify one ADMIN link was deleted but other remains
            assertEquals(initialLinkCount - 1, joinLinkRepository.count());
            assertFalse(joinLinkRepository
                            .findByIdOptional(adminLinkToDelete.getUuid())
                            .isPresent());
            assertTrue(joinLinkRepository
                           .findByIdOptional(secondAdminLink.getUuid())
                           .isPresent());
        }
    }

    // Helper method to create a share with multiple links
    private Share createShareWithLinks(String name, String user, Permissions permissions, int linkCount) {
        Share share = new Share();
        share.setName(name);
        share.setLinks(new ArrayList<>());
        shareRepository.persist(share);

        // Create owner link
        JoinLink ownerLink = new JoinLink();
        ownerLink.setShare(share);
        ownerLink.setName("Owner Link for " + name);
        ownerLink.setUsers(new HashSet<>(Set.of(user))); // Initialize users collection
        ownerLink.setPermissions(permissions);
        ownerLink.setActive(true);
        ownerLink.setSingleUse(false);
        joinLinkRepository.persist(ownerLink);
        share
            .getLinks()
            .add(ownerLink);

        // Create additional links
        for(int i = 1; i < linkCount; i++) {
            JoinLink link = new JoinLink();
            link.setShare(share);
            link.setName("Link " + i + " for " + name);
            link.setUsers(new HashSet<>()); // Initialize users collection
            link.setPermissions(Permissions.READ);
            link.setActive(true);
            link.setSingleUse(false);
            joinLinkRepository.persist(link);
            share
                .getLinks()
                .add(link);
        }

        shareRepository.persist(share);

        return share;
    }
}
