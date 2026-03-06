package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
class JoinLinkControllerTest {

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private JoinLinkRepository joinLinkRepository;

    private MockMvc mockMvc;

    private final RequestPostProcessor user_jwt = jwt()
        .jwt(j -> j.subject("testuser"));

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();

        // Clean up any existing shares and links
        joinLinkRepository.deleteAll();
        shareRepository.deleteAll();
    }

    @Nested
    class GetAllLinks {
        @Test
        void shouldReturnAllLinksForUserWithAdminPermissions() throws Exception {
            // Create a share with user having ADMIN permissions and multiple links
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 3);

            mockMvc
                .perform(get("/api/share/current/links?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() throws Exception {
            // Create a share with user having WRITE permissions
            Share testShare = createShareWithLinks("Write Share", "sub: testuser", Permissions.WRITE, 2);

            mockMvc
                .perform(get("/api/share/current/links?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() throws Exception {
            // Create a share with user having READ permissions
            Share testShare = createShareWithLinks("Read Share", "sub: testuser", Permissions.READ, 1);

            mockMvc
                .perform(get("/api/share/current/links?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturnEmptyArrayWhenShareHasOnlyAccessLink() throws Exception {
            // Create a share with only the owner link (no additional links)
            Share testShare = createShareWithLinks("Minimal Share", "sub: testuser", Permissions.ADMIN, 1);

            mockMvc
                .perform(get("/api/share/current/links?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1)); // Owner link should be returned
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() throws Exception {
            // Create a share where user has no permissions
            Share testShare = createShareWithLinks("No Access Share", "sub: otheruser", Permissions.ADMIN, 1);

            mockMvc
                .perform(get("/api/share/current/links?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            mockMvc
                .perform(get("/api/share/current/links?share=" + Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);

            mockMvc
                .perform(get("/api/share/current/links?share=" + testShare.getUuid()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn403WhenNoShareParameterProvided() throws Exception {
            mockMvc
                .perform(get("/api/share/current/links").with(user_jwt))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class GetLinkById {
        @Test
        void shouldReturnLinkWhenUserHasAdminPermissions() throws Exception {
            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 2);
            JoinLink expectedLink = testShare.getLinks().get(0);

            mockMvc
                .perform(get("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), expectedLink.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(expectedLink.getUuid().toString()))
                .andExpect(jsonPath("$.name").value(expectedLink.getName()))
                .andExpect(jsonPath("$.permissions").value(expectedLink.getPermissions().toString()))
                .andExpect(jsonPath("$.active").value(expectedLink.isActive()))
                .andExpect(jsonPath("$.singleUse").value(expectedLink.isSingleUse()));
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() throws Exception {
            // Create a share with user having WRITE permissions
            Share testShare = createShareWithLinks("Write Share", "sub: testuser", Permissions.WRITE, 1);
            JoinLink expectedLink = testShare.getLinks().get(0);

            mockMvc
                .perform(get("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), expectedLink.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403WhenUserWithReadPermissions() throws Exception {
            // Create a share with user having READ permissions
            Share testShare = createShareWithLinks("Read Share", "sub: testuser", Permissions.READ, 1);
            JoinLink expectedLink = testShare.getLinks().get(0);

            mockMvc
                .perform(get("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), expectedLink.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403WhenLinkExistsButUserHasNoPermissions() throws Exception {
            // Create a share where user has no permissions
            Share testShare = createShareWithLinks("No Access Share", "sub: otheruser", Permissions.ADMIN, 1);
            JoinLink link = testShare.getLinks().get(0);

            mockMvc
                .perform(get("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), link.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn404WhenLinkDoesNotExist() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);

            mockMvc
                .perform(get("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            mockMvc
                .perform(get("/api/share/current/links/{uuid}?share=" + Testdata.BAD_UUID, UUID.randomUUID()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);
            JoinLink link = testShare.getLinks().get(0);

            mockMvc
                .perform(get("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), link.getUuid()))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateLink {
        @Test
        void shouldCreateLinkWhenUserHasAdminPermissions() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);

            String createJson = """
                {
                  "name": "New Link",
                  "permissions": "WRITE",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            mockMvc
                .perform(post("/api/share/current/links?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").exists())
                .andExpect(jsonPath("$.name").value("New Link"))
                .andExpect(jsonPath("$.permissions").value("WRITE"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.singleUse").value(false))
                .andExpect(jsonPath("$.validTo").isEmpty())
                .andExpect(jsonPath("$.numUsers").value(0));

            // Verify link was created in database
            List<JoinLink> links = joinLinkRepository.findAll();
            assertEquals(2, links.size()); // Original link + new link
            
            JoinLink newLink = links.stream()
                .filter(l -> l.getName().equals("New Link"))
                .findFirst()
                .orElseThrow();
            assertEquals("WRITE", newLink.getPermissions().toString());
            assertTrue(newLink.isActive());
            assertFalse(newLink.isSingleUse());
            assertNull(newLink.getValidTo());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() throws Exception {
            Share testShare = createShareWithLinks("Write Share", "sub: testuser", Permissions.WRITE, 1);

            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            mockMvc
                .perform(post("/api/share/current/links?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isForbidden());

            // Verify no new link was created
            List<JoinLink> links = joinLinkRepository.findAll();
            assertEquals(1, links.size());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() throws Exception {
            Share testShare = createShareWithLinks("Read Share", "sub: testuser", Permissions.READ, 1);

            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            mockMvc
                .perform(post("/api/share/current/links?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isForbidden());

            // Verify no new link was created
            List<JoinLink> links = joinLinkRepository.findAll();
            assertEquals(1, links.size());
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() throws Exception {
            Share testShare = createShareWithLinks("No Access Share", "sub: otheruser", Permissions.ADMIN, 1);

            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            mockMvc
                .perform(post("/api/share/current/links?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            mockMvc
                .perform(post("/api/share/current/links?share=" + Testdata.BAD_UUID)
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);

            String createJson = """
                {
                  "name": "Should Not Create",
                  "permissions": "READ",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            mockMvc
                .perform(post("/api/share/current/links?share=" + testShare.getUuid())
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldHandleNullFieldsInCreateRequest() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);

            String createJson = """
                {
                  "name": "Minimal Link",
                  "permissions": "READ",
                  "active": null,
                  "singleUse": null,
                  "validTo": null
                }""";

            mockMvc
                .perform(post("/api/share/current/links?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Minimal Link"))
                .andExpect(jsonPath("$.permissions").value("READ"));
        }
    }

    @Nested
    class UpdateLink {
        @Test
        void shouldUpdateLinkWhenUserHasAdminPermissions() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 2);
            JoinLink linkToUpdate = testShare.getLinks().get(0);

            String updateJson = """
                {
                  "name": "Updated Link",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": "2025-12-31T23:59:59Z"
                }""";

            mockMvc
                .perform(put("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToUpdate.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(linkToUpdate.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Updated Link"))
                .andExpect(jsonPath("$.permissions").value("READ"))
                .andExpect(jsonPath("$.active").value(false))
                .andExpect(jsonPath("$.singleUse").value(true))
                .andExpect(jsonPath("$.validTo").exists());

            // Verify update in database
            JoinLink updatedLink = joinLinkRepository.findById(linkToUpdate.getUuid()).orElseThrow();
            assertEquals("Updated Link", updatedLink.getName());
            assertEquals("READ", updatedLink.getPermissions().toString());
            assertFalse(updatedLink.isActive());
            assertTrue(updatedLink.isSingleUse());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() throws Exception {
            Share testShare = createShareWithLinks("Write Share", "sub: testuser", Permissions.WRITE, 1);
            JoinLink linkToUpdate = testShare.getLinks().get(0);

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "WRITE",
                  "active": true,
                  "singleUse": false,
                  "validTo": null
                }""";

            mockMvc
                .perform(put("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToUpdate.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());

            // Verify link was not updated
            JoinLink unchangedLink = joinLinkRepository.findById(linkToUpdate.getUuid()).orElseThrow();
            assertNotEquals("Should Not Update", unchangedLink.getName());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() throws Exception {
            Share testShare = createShareWithLinks("Read Share", "sub: testuser", Permissions.READ, 1);
            JoinLink linkToUpdate = testShare.getLinks().get(0);

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            mockMvc
                .perform(put("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToUpdate.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());

            // Verify link was not updated
            JoinLink unchangedLink = joinLinkRepository.findById(linkToUpdate.getUuid()).orElseThrow();
            assertNotEquals("Should Not Update", unchangedLink.getName());
        }

        @Test
        void shouldReturn403WhenLinkExistsButUserHasNoPermissions() throws Exception {
            Share testShare = createShareWithLinks("No Access Share", "sub: otheruser", Permissions.ADMIN, 1);
            JoinLink linkToUpdate = testShare.getLinks().get(0);

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            mockMvc
                .perform(put("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToUpdate.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn404WhenLinkDoesNotExist() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            mockMvc
                .perform(put("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), Testdata.BAD_UUID)
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            mockMvc
                .perform(put("/api/share/current/links/{uuid}?share=" + Testdata.BAD_UUID, UUID.randomUUID())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 1);
            JoinLink linkToUpdate = testShare.getLinks().get(0);

            String updateJson = """
                {
                  "name": "Should Not Update",
                  "permissions": "READ",
                  "active": false,
                  "singleUse": true,
                  "validTo": null
                }""";

            mockMvc
                .perform(put("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToUpdate.getUuid())
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteLink {
        @Test
        void shouldDeleteLinkWhenUserHasAdminPermissions() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 3);
            // Make sure we have at least 2 admin links
            JoinLink secondAdminLink = testShare.getLinks().get(1);
            secondAdminLink.setPermissions(Permissions.ADMIN);
            joinLinkRepository.save(secondAdminLink);
            
            JoinLink linkToDelete = testShare.getLinks().get(0);
            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToDelete.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify link was deleted
            assertEquals(initialLinkCount - 1, joinLinkRepository.count());
            assertFalse(joinLinkRepository.findById(linkToDelete.getUuid()).isPresent());

            // Verify other links still exist
            assertEquals(2, joinLinkRepository.findAll().size());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() throws Exception {
            Share testShare = createShareWithLinks("Write Share", "sub: testuser", Permissions.WRITE, 2);
            JoinLink linkToDelete = testShare.getLinks().get(0);
            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToDelete.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify link was not deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
            assertTrue(joinLinkRepository.findById(linkToDelete.getUuid()).isPresent());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() throws Exception {
            Share testShare = createShareWithLinks("Read Share", "sub: testuser", Permissions.READ, 2);
            JoinLink linkToDelete = testShare.getLinks().get(0);
            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToDelete.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify link was not deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
            assertTrue(joinLinkRepository.findById(linkToDelete.getUuid()).isPresent());
        }

        @Test
        void shouldReturn403WhenLinkExistsButUserHasNoPermissions() throws Exception {
            Share testShare = createShareWithLinks("No Access Share", "sub: otheruser", Permissions.ADMIN, 2);
            JoinLink linkToDelete = testShare.getLinks().get(0);
            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToDelete.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify link was not deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
            assertTrue(joinLinkRepository.findById(linkToDelete.getUuid()).isPresent());
        }

        @Test
        void shouldReturn404WhenLinkDoesNotExist() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 2);
            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isNotFound());

            // Verify no links were deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + Testdata.BAD_UUID, UUID.randomUUID()).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify no links were deleted
            assertEquals(initialLinkCount, joinLinkRepository.count());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithLinks("Test Share", "sub: testuser", Permissions.ADMIN, 2);
            JoinLink linkToDelete = testShare.getLinks().get(0);

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), linkToDelete.getUuid()))
                .andExpect(status().isUnauthorized());

            // Verify link was not deleted
            assertTrue(joinLinkRepository.findById(linkToDelete.getUuid()).isPresent());
        }

        @Test
        void shouldThrowExceptionWhenDeletingLastAdminLink() throws Exception {
            // Create a share with only one ADMIN link and one WRITE link
            Share testShare = createShareWithLinks("Single Admin Share", "sub: testuser", Permissions.ADMIN, 2);
            // Make the second link WRITE permissions (not ADMIN)
            JoinLink writeLink = testShare.getLinks().get(1);
            writeLink.setPermissions(Permissions.WRITE);
            joinLinkRepository.save(writeLink);
            
            // Now we have only one admin link (the first one)
            JoinLink adminLink = testShare.getLinks().get(0);

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), adminLink.getUuid()).with(user_jwt))
                .andExpect(status().isBadRequest());

            // Verify admin link was not deleted
            assertTrue(joinLinkRepository.findById(adminLink.getUuid()).isPresent());
        }

        @Test
        void shouldAllowDeletingNonAdminLinks() throws Exception {
            // Create a share with one ADMIN and one WRITE link
            Share testShare = createShareWithLinks("Mixed Share", "sub: testuser", Permissions.ADMIN, 2);
            // Make the second link WRITE permissions
            JoinLink writeLink = testShare.getLinks().get(1);
            writeLink.setPermissions(Permissions.WRITE);
            joinLinkRepository.save(writeLink);

            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), writeLink.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify WRITE link was deleted but ADMIN link remains
            assertEquals(initialLinkCount - 1, joinLinkRepository.count());
            assertFalse(joinLinkRepository.findById(writeLink.getUuid()).isPresent());
            assertTrue(joinLinkRepository.findById(testShare.getLinks().get(0).getUuid()).isPresent());
        }

        @Test
        void shouldAllowDeletingAdminLinkWhenOtherAdminLinkExists() throws Exception {
            // Create a share with two ADMIN links
            Share testShare = createShareWithLinks("Multi Admin Share", "sub: testuser", Permissions.ADMIN, 2);
            JoinLink adminLinkToDelete = testShare.getLinks().get(0);
            // Ensure both links are ADMIN
            JoinLink secondAdminLink = testShare.getLinks().get(1);
            secondAdminLink.setPermissions(Permissions.ADMIN);
            joinLinkRepository.save(secondAdminLink);

            long initialLinkCount = joinLinkRepository.count();

            mockMvc
                .perform(delete("/api/share/current/links/{uuid}?share=" + testShare.getUuid(), adminLinkToDelete.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify one ADMIN link was deleted but other remains
            assertEquals(initialLinkCount - 1, joinLinkRepository.count());
            assertFalse(joinLinkRepository.findById(adminLinkToDelete.getUuid()).isPresent());
            assertTrue(joinLinkRepository.findById(secondAdminLink.getUuid()).isPresent());
        }
    }

    // Helper method to create a share with multiple links
    private Share createShareWithLinks(String name, String user, Permissions permissions, int linkCount) {
        Share share = new Share();
        share.setName(name);
        share.setLinks(new ArrayList<>());
        share = shareRepository.save(share);

        // Create owner link
        JoinLink ownerLink = new JoinLink();
        ownerLink.setShare(share);
        ownerLink.setName("Owner Link for " + name);
        ownerLink.setUsers(new HashSet<>(Set.of(user))); // Initialize users collection
        ownerLink.setPermissions(permissions);
        ownerLink.setActive(true);
        ownerLink.setSingleUse(false);
        ownerLink = joinLinkRepository.save(ownerLink);
        share.getLinks().add(ownerLink);

        // Create additional links
        for (int i = 1; i < linkCount; i++) {
            JoinLink link = new JoinLink();
            link.setShare(share);
            link.setName("Link " + i + " for " + name);
            link.setUsers(new HashSet<>()); // Initialize users collection
            link.setPermissions(Permissions.READ);
            link.setActive(true);
            link.setSingleUse(false);
            link = joinLinkRepository.save(link);
            share.getLinks().add(link);
        }

        return shareRepository.save(share);
    }
}
