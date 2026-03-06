package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import org.hamcrest.CustomMatcher;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
class ShareControllerTest {

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private JoinLinkRepository joinLinkRepository;

    private MockMvc mockMvc;

    private final RequestPostProcessor user_jwt = jwt()
        .jwt(j -> j.subject("testuser"));

    private final RequestPostProcessor other_user_jwt = jwt()
        .jwt(j -> j.subject("otheruser"));

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();

        // Clean up any existing shares and join links
        joinLinkRepository.deleteAll();
        shareRepository.deleteAll();
    }

    @Nested
    class CreateShare {
        @Test
        void shouldCreateShareWhenAuthorized() throws Exception {
            long initialShareCount = shareRepository.count();
            long initialLinkCount = joinLinkRepository.count();

            String createJson = """
                {
                  "name": "Test Share"
                }""";

            mockMvc
                .perform(post("/api/share")
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Share"))
                .andExpect(jsonPath("$.permissions").value("ADMIN"))
                .andExpect(jsonPath("$.uuid").value(new CustomMatcher<UUID>("Valid UUID that exists in database") {
                    @Override
                    public boolean matches(Object actual) {
                        UUID uuid;
                        if(actual instanceof UUID u) uuid = u;
                        else if(actual instanceof String s) uuid = UUID.fromString(s);
                        else {
                            fail("Expected UUID or String but got: " + actual.getClass().getSimpleName());
                            return false;
                        }
                        Share createdShare = shareRepository.findById(uuid).orElse(null);

                        assertNotNull(createdShare);
                        // Verify the share and owner link were created correctly
                        assertEquals("Test Share", createdShare.getName());
                        assertEquals(1, createdShare.getLinks().size());
                        JoinLink ownerLink = createdShare
                            .getLinks()
                            .get(0);
                        assertEquals(createdShare, ownerLink.getShare());
                        assertEquals(Permissions.ADMIN, ownerLink.getPermissions());
                        assertTrue(ownerLink.getUsers().contains("sub: testuser"));
                        assertFalse(ownerLink.isActive()); // Owner links are inactive by default
                        return true;
                    }
                }));

            // Verify creation
            assertEquals(initialShareCount + 1, shareRepository.count());
            assertEquals(initialLinkCount + 1, joinLinkRepository.count());
        }

        @Test
        void shouldCreateShareWithDifferentName() throws Exception {
            String createJson = """
                {
                  "name": "Family Shopping List"
                }""";

            mockMvc
                .perform(post("/api/share")
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Family Shopping List"))
                .andExpect(jsonPath("$.permissions").value("ADMIN"))
                .andExpect(jsonPath("$.uuid").value(new CustomMatcher<UUID>("Valid UUID that exists in database") {
                    @Override
                    public boolean matches(Object actual) {
                        UUID uuid;
                        if(actual instanceof UUID u) uuid = u;
                        else if(actual instanceof String s) uuid = UUID.fromString(s);
                        else {
                            fail("Expected UUID or String but got: " + actual.getClass().getSimpleName());
                            return false;
                        }
                        Share createdShare = shareRepository.findById(uuid).orElse(null);
                        assertNotNull(createdShare);
                        assertEquals("Family Shopping List", createdShare.getName());
                        assertEquals(1, createdShare.getLinks().size());
                        return true;
                    }
                }));
        }
    }

    @Nested
    class JoinShare {
        @Test
        void shouldJoinShareWhenLinkExistsAndActive() throws Exception {
            // Create a share and join link first
            Share testShare = new Share();
            testShare.setName("Test Share");
            testShare.setLinks(new ArrayList<>());
            testShare = shareRepository.save(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Test Link");
            joinLink.setPermissions(Permissions.WRITE);
            joinLink.setActive(true);
            joinLink.setSingleUse(false);
            joinLink.setUsers(new HashSet<>(Set.of("sub: otheruser"))); // Another user already joined
            joinLink = joinLinkRepository.save(joinLink);
            
            // Add the link to the share's links list
            testShare.getLinks().add(joinLink);
            shareRepository.save(testShare);

            mockMvc
                .perform(post("/api/share/join/{uuid}", joinLink.getUuid())
                             .with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Test Share"))
                .andExpect(jsonPath("$.permissions").value("WRITE"))
                .andExpect(jsonPath("$.uuid").value(testShare.getUuid().toString()));

            // Verify user was added to the join link
            JoinLink updatedLink = joinLinkRepository.findById(joinLink.getUuid()).orElseThrow();
            assertTrue(updatedLink.getUsers().contains("sub: testuser"));
            assertTrue(updatedLink.getUsers().contains("sub: otheruser"));
        }

        @Test
        void shouldJoinShareWithSingleUseLinkAndDeactivateIt() throws Exception {
            // Create a share and single-use join link
            Share testShare = new Share();
            testShare.setName("Single Use Share");
            testShare.setLinks(new ArrayList<>());
            testShare = shareRepository.save(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Single Use Link");
            joinLink.setPermissions(Permissions.READ);
            joinLink.setActive(true);
            joinLink.setSingleUse(true);
            joinLink.setUsers(new HashSet<>());
            joinLink = joinLinkRepository.save(joinLink);
            
            // Add the link to the share's links list
            testShare.getLinks().add(joinLink);
            shareRepository.save(testShare);

            mockMvc
                .perform(post("/api/share/join/{uuid}", joinLink.getUuid())
                             .with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Single Use Share"))
                .andExpect(jsonPath("$.permissions").value("READ"))
                .andExpect(jsonPath("$.uuid").value(testShare.getUuid().toString()));

            // Verify link was deactivated after use
            JoinLink updatedLink = joinLinkRepository.findById(joinLink.getUuid()).orElseThrow();
            assertFalse(updatedLink.isActive());
            assertTrue(updatedLink.getUsers().contains("sub: testuser"));
        }

        @Test
        void shouldReturn404WhenJoinLinkNotFound() throws Exception {
            mockMvc
                .perform(post("/api/share/join/{uuid}", Testdata.BAD_UUID)
                             .with(user_jwt))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenJoinLinkInactive() throws Exception {
            // Create a share and inactive join link
            Share testShare = new Share();
            testShare.setName("Inactive Share");
            testShare.setLinks(new ArrayList<>());
            testShare = shareRepository.save(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Inactive Link");
            joinLink.setPermissions(Permissions.READ);
            joinLink.setActive(false); // Inactive link
            joinLink.setSingleUse(false);
            joinLink.setUsers(new HashSet<>());
            joinLink = joinLinkRepository.save(joinLink);
            
            // Add the link to the share's links list
            testShare.getLinks().add(joinLink);
            shareRepository.save(testShare);

            mockMvc
                .perform(post("/api/share/join/{uuid}", joinLink.getUuid())
                             .with(user_jwt))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenJoinLinkExpired() throws Exception {
            // Create a share and expired join link
            Share testShare = new Share();
            testShare.setName("Expired Share");
            testShare.setLinks(new ArrayList<>());
            testShare = shareRepository.save(testShare);

            JoinLink joinLink = new JoinLink();
            joinLink.setShare(testShare);
            joinLink.setName("Expired Link");
            joinLink.setPermissions(Permissions.READ);
            joinLink.setActive(true);
            joinLink.setSingleUse(false);
            joinLink.setValidTo(java.time.Instant.now().minusSeconds(3600)); // Expired 1 hour ago
            joinLink.setUsers(new HashSet<>());
            joinLink = joinLinkRepository.save(joinLink);
            
            // Add the link to the share's links list
            testShare.getLinks().add(joinLink);
            shareRepository.save(testShare);

            mockMvc
                .perform(post("/api/share/join/{uuid}", joinLink.getUuid())
                             .with(user_jwt))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetAllShares {
        @Test
        void shouldReturnUserSharesOnly() throws Exception {
            // Create shares for current user
            Share userShare1 = createShareWithOwner("User Share 1", "sub: testuser", Permissions.ADMIN);
            Share userShare2 = createShareWithOwner("User Share 2", "sub: testuser", Permissions.WRITE);
            
            // Create shares for different user
            createShareWithOwner("Other Share 1", "sub: otheruser", Permissions.ADMIN);
            createShareWithOwner("Other Share 2", "sub: otheruser", Permissions.READ);

            mockMvc
                .perform(get("/api/share").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.name == 'User Share 1' && @.uuid == '" + userShare1.getUuid().toString() + "')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'User Share 2' && @.uuid == '" + userShare2.getUuid().toString() + "')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Other Share 1')]").doesNotExist())
                .andExpect(jsonPath("$[?(@.name == 'Other Share 2')]").doesNotExist());
        }

        @Test
        void shouldReturnEmptyWhenUserHasNoShares() throws Exception {
            // Create shares for different user only
            createShareWithOwner("Other Share 1", "sub: otheruser", Permissions.ADMIN);
            createShareWithOwner("Other Share 2", "sub: otheruser", Permissions.READ);

            mockMvc
                .perform(get("/api/share").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void shouldReturnCorrectPermissionsForUser() throws Exception {
            // Create shares with different permission levels for current user
            Share adminShare = createShareWithOwner("Admin Share", "sub: testuser", Permissions.ADMIN);
            Share writeShare = createShareWithOwner("Write Share", "sub: testuser", Permissions.WRITE);
            Share readShare = createShareWithOwner("Read Share", "sub: testuser", Permissions.READ);

            mockMvc
                .perform(get("/api/share").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.name == 'Admin Share' && @.permissions == 'ADMIN' && @.uuid == '" + adminShare.getUuid().toString() + "')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Write Share' && @.permissions == 'WRITE' && @.uuid == '" + writeShare.getUuid().toString() + "')]").exists())
                .andExpect(jsonPath("$[?(@.name == 'Read Share' && @.permissions == 'READ' && @.uuid == '" + readShare.getUuid().toString() + "')]").exists());
        }

        @Test
        void shouldReturnOnlyUserSharesWhenMultipleUsersExist() throws Exception {
            // Create shares for current user
            createShareWithOwner("User Share 1", "sub: testuser", Permissions.ADMIN);
            createShareWithOwner("User Share 2", "sub: testuser", Permissions.WRITE);
            
            // Create shares for different user
            Share otherShare1 = createShareWithOwner("Other Share 1", "sub: otheruser", Permissions.ADMIN);
            
            // Test with other user JWT
            mockMvc
                .perform(get("/api/share").with(other_user_jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[?(@.name == 'Other Share 1' && @.uuid == '" + otherShare1.getUuid().toString() + "')]").exists());
        }
    }

    // Helper method to create a share with an owner join link
    private Share createShareWithOwner(String name, String user, Permissions permissions) {
        Share share = new Share();
        share.setName(name);
        share.setLinks(new ArrayList<>()); // Initialize the links list
        share = shareRepository.save(share);

        JoinLink ownerLink = new JoinLink();
        ownerLink.setShare(share);
        ownerLink.setName("Owner Link for " + name);
        ownerLink.setUsers(new HashSet<>(Set.of(user)));
        ownerLink.setPermissions(permissions);
        ownerLink.setActive(true);
        ownerLink.setSingleUse(false);
        ownerLink = joinLinkRepository.save(ownerLink);
        
        // Add the link to the share's links list
        share.getLinks().add(ownerLink);
        shareRepository.save(share);

        return share;
    }
}
