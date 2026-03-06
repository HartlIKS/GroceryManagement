package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.model.ProductGroup;
import de.iks.grocery_manager.server.model.ShoppingList;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
class CurrentShareControllerTest {

    @Autowired
    private ShareRepository shareRepository;

    @Autowired
    private ProductGroupRepository productGroupRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingTripRepository shoppingTripRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    private MockMvc mockMvc;

    private final RequestPostProcessor user_jwt = jwt()
        .jwt(j -> j.subject("testuser"));

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();

        // Clean up any existing shares and related data
        shareRepository.deleteAll();
        productGroupRepository.deleteAll();
        shoppingListRepository.deleteAll();
        shoppingTripRepository.deleteAll();
    }

    @Nested
    class GetCurrentShare {
        @Test
        void shouldReturnCurrentShareWhenShareParameterProvided() throws Exception {
            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.ADMIN);

            mockMvc
                .perform(get("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testShare.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Test Share"))
                .andExpect(jsonPath("$.permissions").value("ADMIN"));
        }

        @Test
        void shouldReturnCurrentShareWithWritePermissions() throws Exception {
            // Create a share with user having WRITE permissions
            Share testShare = createShareWithOwner("Write Share", "sub: testuser", Permissions.WRITE);

            mockMvc
                .perform(get("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(testShare.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Write Share"))
                .andExpect(jsonPath("$.permissions").value("WRITE"));
        }

        @Test
        void shouldReturnCurrentShareWithReadPermissions() throws Exception {
            // Create a share with user having READ permissions
            Share testShare = createShareWithOwner("Read Share", "sub: testuser", Permissions.READ);

            mockMvc
                .perform(get("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(testShare.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Read Share"))
                .andExpect(jsonPath("$.permissions").value("READ"));
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() throws Exception {
            // Create a share where user has no permissions
            Share testShare = createShareWithOwner("No Access Share", "sub: otheruser", Permissions.ADMIN);

            mockMvc
                .perform(get("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            mockMvc
                .perform(get("/api/share/current?share=" + Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.ADMIN);

            mockMvc
                .perform(get("/api/share/current?share=" + testShare.getUuid()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn403WhenNoShareParameterProvided() throws Exception {
            mockMvc
                .perform(get("/api/share/current").with(user_jwt))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class UpdateCurrentShare {
        @Test
        void shouldUpdateCurrentShareWhenUserHasAdminPermissions() throws Exception {
            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithOwner("Original Name", "sub: testuser", Permissions.ADMIN);

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            mockMvc
                .perform(put("/api/share/current?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testShare.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.permissions").value("ADMIN"));

            // Verify update in database
            Share updatedShare = shareRepository.findById(testShare.getUuid()).orElseThrow();
            assertEquals("Updated Name", updatedShare.getName());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() throws Exception {
            // Create a share with user having only WRITE permissions
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.WRITE);

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            mockMvc
                .perform(put("/api/share/current?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());

            // Verify share was not updated
            Share unchangedShare = shareRepository.findById(testShare.getUuid()).orElseThrow();
            assertEquals("Test Share", unchangedShare.getName());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() throws Exception {
            // Create a share with user having only READ permissions
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.READ);

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            mockMvc
                .perform(put("/api/share/current?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());

            // Verify share was not updated
            Share unchangedShare = shareRepository.findById(testShare.getUuid()).orElseThrow();
            assertEquals("Test Share", unchangedShare.getName());
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() throws Exception {
            // Create a share where user has no permissions
            Share testShare = createShareWithOwner("Test Share", "sub: otheruser", Permissions.ADMIN);

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            mockMvc
                .perform(put("/api/share/current?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());

            // Verify share was not updated
            Share unchangedShare = shareRepository.findById(testShare.getUuid()).orElseThrow();
            assertEquals("Test Share", unchangedShare.getName());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            mockMvc
                .perform(put("/api/share/current?share=" + Testdata.BAD_UUID)
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.ADMIN);

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            mockMvc
                .perform(put("/api/share/current?share=" + testShare.getUuid())
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldHandleEmptyNameUpdate() throws Exception {
            Share testShare = createShareWithOwner("Original Name", "sub: testuser", Permissions.ADMIN);

            String updateJson = """
                {
                  "name": ""
                }""";

            mockMvc
                .perform(put("/api/share/current?share=" + testShare.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(""));

            // Verify update in database
            Share updatedShare = shareRepository.findById(testShare.getUuid()).orElseThrow();
            assertEquals("", updatedShare.getName());
        }
    }

    @Nested
    class DeleteCurrentShare {
        @Test
        void shouldDeleteCurrentShareAndCleanupWhenUserHasAdminPermissions() throws Exception {
            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.ADMIN);

            // Create some related data for this share
            createTestDataForShare(testShare);

            long initialShareCount = shareRepository.count();
            long initialGroupCount = productGroupRepository.count();
            long initialListCount = shoppingListRepository.count();
            long initialTripCount = shoppingTripRepository.count();

            mockMvc
                .perform(delete("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify share was deleted
            assertEquals(initialShareCount - 1, shareRepository.count());
            assertFalse(shareRepository.findById(testShare.getUuid()).isPresent());

            // Verify cleanup of related data
            assertEquals(initialGroupCount - 1, productGroupRepository.count());
            assertEquals(initialListCount - 1, shoppingListRepository.count());
            assertEquals(initialTripCount - 1, shoppingTripRepository.count());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() throws Exception {
            // Create a share with user having only WRITE permissions
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.WRITE);

            // Create some related data for this share
            createTestDataForShare(testShare);

            long initialShareCount = shareRepository.count();
            long initialGroupCount = productGroupRepository.count();

            mockMvc
                .perform(delete("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify share was not deleted
            assertEquals(initialShareCount, shareRepository.count());
            assertTrue(shareRepository.findById(testShare.getUuid()).isPresent());
            assertEquals(initialGroupCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() throws Exception {
            // Create a share with user having only READ permissions
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.READ);

            long initialShareCount = shareRepository.count();

            mockMvc
                .perform(delete("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify share was not deleted
            assertEquals(initialShareCount, shareRepository.count());
            assertTrue(shareRepository.findById(testShare.getUuid()).isPresent());
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() throws Exception {
            // Create a share where user has no permissions
            Share testShare = createShareWithOwner("Test Share", "sub: otheruser", Permissions.ADMIN);

            long initialShareCount = shareRepository.count();

            mockMvc
                .perform(delete("/api/share/current?share=" + testShare.getUuid()).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify share was not deleted
            assertEquals(initialShareCount, shareRepository.count());
            assertTrue(shareRepository.findById(testShare.getUuid()).isPresent());
        }

        @Test
        void shouldReturn403WhenShareNotFound() throws Exception {
            long initialShareCount = shareRepository.count();

            mockMvc
                .perform(delete("/api/share/current?share=" + Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isForbidden());

            // Verify no shares were deleted
            assertEquals(initialShareCount, shareRepository.count());
        }

        @Test
        void shouldReturn401WhenNoAuthentication() throws Exception {
            Share testShare = createShareWithOwner("Test Share", "sub: testuser", Permissions.ADMIN);

            mockMvc
                .perform(delete("/api/share/current?share=" + testShare.getUuid()))
                .andExpect(status().isUnauthorized());

            // Verify share was not deleted
            assertTrue(shareRepository.findById(testShare.getUuid()).isPresent());
        }

        @Test
        void shouldOnlyCleanupDataForDeletedShareOwner() throws Exception {
            // Create two shares with different owners
            Share testShare1 = createShareWithOwner("Test Share 1", "sub: testuser", Permissions.ADMIN);
            Share testShare2 = createShareWithOwner("Test Share 2", "sub: otheruser", Permissions.ADMIN);

            // Create data for both shares
            createTestDataForShare(testShare1);
            createTestDataForShare(testShare2);

            long initialShareCount = shareRepository.count();
            long initialGroupCount = productGroupRepository.count();
            long initialListCount = shoppingListRepository.count();
            long initialTripCount = shoppingTripRepository.count();

            // Delete first share
            mockMvc
                .perform(delete("/api/share/current?share=" + testShare1.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify only first share and its data were deleted
            assertEquals(initialShareCount - 1, shareRepository.count());
            assertFalse(shareRepository.findById(testShare1.getUuid()).isPresent());
            assertTrue(shareRepository.findById(testShare2.getUuid()).isPresent());

            // Verify cleanup only for first share's owner
            assertEquals(initialGroupCount - 1, productGroupRepository.count());
            assertEquals(initialListCount - 1, shoppingListRepository.count());
            assertEquals(initialTripCount - 1, shoppingTripRepository.count());
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
        
        // Add the link to the share's links list
        share.getLinks().add(ownerLink);
        shareRepository.save(share);

        return share;
    }

    // Helper method to create test data for a share
    private void createTestDataForShare(Share share) {
        // Use the same owner format as getOwner would return for SharePrincipal
        String shareOwner = String.format("share: %s", share.getUuid());
        
        // Create a product group
        Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
        ProductGroup group = new ProductGroup();
        group.setName("Test Group");
        group.setOwner(shareOwner);
        group.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.0"))));
        productGroupRepository.save(group);

        // Create a shopping list
        ShoppingList list = new ShoppingList();
        list.setName("Test List");
        list.setOwner(shareOwner);
        list.setRepeating(false);
        list.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
        list.setProductGroups(new HashMap<>());
        shoppingListRepository.save(list);

        // Create a shopping trip with required store
        Store store = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
        ShoppingTrip trip = new ShoppingTrip();
        trip.setStore(store);
        trip.setOwner(shareOwner);
        trip.setTime(java.time.Instant.now());
        trip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("3.0"))));
        shoppingTripRepository.save(trip);
    }
}
