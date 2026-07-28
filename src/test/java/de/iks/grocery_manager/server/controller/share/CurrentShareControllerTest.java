package de.iks.grocery_manager.server.controller.share;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import de.iks.grocery_manager.server.model.ProductGroup;
import de.iks.grocery_manager.server.model.ShoppingList;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import de.iks.grocery_manager.server.model.share.JoinLink;
import de.iks.grocery_manager.server.model.share.Permissions;
import de.iks.grocery_manager.server.model.share.Share;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static de.iks.grocery_manager.server.UUIDMatcher.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(CurrentShareController.class)
@WithTestUser
@Sql("/testdata.sql")
class CurrentShareControllerTest {

    @Inject
    ShareRepository shareRepository;

    @Inject
    ProductGroupRepository productGroupRepository;

    @Inject
    ShoppingListRepository shoppingListRepository;

    @Inject
    ShoppingTripRepository shoppingTripRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    StoreRepository storeRepository;

    @Nested
    @TestHTTPEndpoint(CurrentShareController.class)
    @WithTestUser
    class GetCurrentShare {
        @Test
        void shouldReturnCurrentShareWhenShareParameterProvided() {
            QuarkusTransaction.begin();

            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser.OWNER, Permissions.ADMIN);

            shareRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(testShare))
                .body("name", is("Test Share"))
                .body("permissions", is("ADMIN"))
                .given()
                .queryParam("share", testShare.getUuid())
                .get();
        }

        @Test
        void shouldReturnCurrentShareWithWritePermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having WRITE permissions
            Share testShare = createShareWithOwner("Write Share", WithTestUser.OWNER, Permissions.WRITE);

            shareRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(testShare))
                .body("name", is("Write Share"))
                .body("permissions", is("WRITE"))
                .given()
                .queryParam("share", testShare.getUuid())
                .get();
        }

        @Test
        void shouldReturnCurrentShareWithReadPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having READ permissions
            Share testShare = createShareWithOwner("Read Share", WithTestUser.OWNER, Permissions.READ);

            shareRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(testShare))
                .body("name", is("Read Share"))
                .body("permissions", is("READ"))
                .given()
                .queryParam("share", testShare.getUuid())
                .get();
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() {
            QuarkusTransaction.begin();

            // Create a share where user has no permissions
            Share testShare = createShareWithOwner("No Access Share", WithTestUser2.OWNER, Permissions.ADMIN);

            shareRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .get();
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            expect()
                .statusCode(403)
                .given()
                .queryParam("share", UUID.randomUUID())
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
    @TestHTTPEndpoint(CurrentShareController.class)
    @WithTestUser
    class UpdateCurrentShare {
        @Test
        void shouldUpdateCurrentShareWhenUserHasAdminPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithOwner("Original Name", WithTestUser.OWNER, Permissions.ADMIN);

            shareRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(testShare))
                .body("name", is("Updated Name"))
                .body("permissions", is("ADMIN"))
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .put();

            // Verify update in database
            Share updatedShare = shareRepository
                .findByIdOptional(testShare.getUuid())
                .orElseThrow();
            assertEquals("Updated Name", updatedShare.getName());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having only WRITE permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser.OWNER, Permissions.WRITE);

            shareRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .put();

            // Verify share was not updated
            Share unchangedShare = shareRepository
                .findByIdOptional(testShare.getUuid())
                .orElseThrow();
            assertEquals("Test Share", unchangedShare.getName());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having only READ permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser.OWNER, Permissions.READ);

            shareRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .put();

            // Verify share was not updated
            Share unchangedShare = shareRepository
                .findByIdOptional(testShare.getUuid())
                .orElseThrow();
            assertEquals("Test Share", unchangedShare.getName());
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() {
            QuarkusTransaction.begin();

            // Create a share where user has no permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser2.OWNER, Permissions.ADMIN);

            shareRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .put();

            // Verify share was not updated
            Share unchangedShare = shareRepository
                .findByIdOptional(testShare.getUuid())
                .orElseThrow();
            assertEquals("Test Share", unchangedShare.getName());
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            String updateJson = """
                {
                  "name": "Updated Name"
                }""";

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", Testdata.BAD_UUID)
                .body(updateJson)
                .contentType(ContentType.JSON)
                .put();
        }

        @Test
        void shouldHandleEmptyNameUpdate() {
            QuarkusTransaction.begin();

            Share testShare = createShareWithOwner("Original Name", WithTestUser.OWNER, Permissions.ADMIN);

            shareRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": ""
                }""";

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", is(""))
                .given()
                .queryParam("share", testShare.getUuid())
                .body(updateJson)
                .contentType(ContentType.JSON)
                .put();

            // Verify update in database
            Share updatedShare = shareRepository
                .findByIdOptional(testShare.getUuid())
                .orElseThrow();
            assertEquals("", updatedShare.getName());
        }
    }

    @Nested
    @TestHTTPEndpoint(CurrentShareController.class)
    @WithTestUser
    class DeleteCurrentShare {
        @Test
        void shouldDeleteCurrentShareAndCleanupWhenUserHasAdminPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having ADMIN permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser.OWNER, Permissions.ADMIN);

            // Create some related data for this share
            createTestDataForShare(testShare);

            long initialShareCount = shareRepository.count();
            long initialGroupCount = productGroupRepository.count();
            long initialListCount = shoppingListRepository.count();
            long initialTripCount = shoppingTripRepository.count();

            shareRepository.flush();
            productGroupRepository.flush();
            shoppingListRepository.flush();
            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .given()
                .queryParam("share", testShare.getUuid())
                .delete();

            // Verify share was deleted
            assertEquals(initialShareCount - 1, shareRepository.count());
            assertFalse(shareRepository
                            .findByIdOptional(testShare.getUuid())
                            .isPresent());

            // Verify cleanup of related data
            assertEquals(initialGroupCount - 1, productGroupRepository.count());
            assertEquals(initialListCount - 1, shoppingListRepository.count());
            assertEquals(initialTripCount - 1, shoppingTripRepository.count());
        }

        @Test
        void shouldReturn403WhenUserHasWritePermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having only WRITE permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser.OWNER, Permissions.WRITE);

            // Create some related data for this share
            createTestDataForShare(testShare);

            long initialShareCount = shareRepository.count();
            long initialGroupCount = productGroupRepository.count();

            shareRepository.flush();
            productGroupRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .delete();

            // Verify share was not deleted
            assertEquals(initialShareCount, shareRepository.count());
            assertTrue(shareRepository
                           .findByIdOptional(testShare.getUuid())
                           .isPresent());
            assertEquals(initialGroupCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn403WhenUserHasReadPermissions() {
            QuarkusTransaction.begin();

            // Create a share with user having only READ permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser.OWNER, Permissions.READ);

            long initialShareCount = shareRepository.count();

            shareRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .delete();

            // Verify share was not deleted
            assertEquals(initialShareCount, shareRepository.count());
            assertTrue(shareRepository
                           .findByIdOptional(testShare.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn403WhenUserHasNoPermissions() {
            QuarkusTransaction.begin();

            // Create a share where user has no permissions
            Share testShare = createShareWithOwner("Test Share", WithTestUser2.OWNER, Permissions.ADMIN);

            long initialShareCount = shareRepository.count();

            shareRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", testShare.getUuid())
                .delete();

            // Verify share was not deleted
            assertEquals(initialShareCount, shareRepository.count());
            assertTrue(shareRepository
                           .findByIdOptional(testShare.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn403WhenShareNotFound() {
            long initialShareCount = shareRepository.count();

            expect()
                .statusCode(403)
                .given()
                .queryParam("share", Testdata.BAD_UUID)
                .delete();

            // Verify no shares were deleted
            assertEquals(initialShareCount, shareRepository.count());
        }

        @Test
        void shouldOnlyCleanupDataForDeletedShareOwner() {
            QuarkusTransaction.begin();

            // Create two shares with different owners
            Share testShare1 = createShareWithOwner("Test Share 1", WithTestUser.OWNER, Permissions.ADMIN);
            Share testShare2 = createShareWithOwner("Test Share 2", WithTestUser2.OWNER, Permissions.ADMIN);

            // Create data for both shares
            createTestDataForShare(testShare1);
            createTestDataForShare(testShare2);

            long initialShareCount = shareRepository.count();
            long initialGroupCount = productGroupRepository.count();
            long initialListCount = shoppingListRepository.count();
            long initialTripCount = shoppingTripRepository.count();

            shareRepository.flush();
            productGroupRepository.flush();
            shoppingListRepository.flush();
            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .given()
                .queryParam("share", testShare1.getUuid())
                .delete();

            // Verify only first share and its data were deleted
            assertEquals(initialShareCount - 1, shareRepository.count());
            assertFalse(shareRepository
                            .findByIdOptional(testShare1.getUuid())
                            .isPresent());
            assertTrue(shareRepository
                           .findByIdOptional(testShare2.getUuid())
                           .isPresent());

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
        shareRepository.persist(share);

        JoinLink ownerLink = new JoinLink();
        ownerLink.setShare(share);
        ownerLink.setName("Owner Link for " + name);
        ownerLink.setUsers(new HashSet<>(Set.of(user)));
        ownerLink.setPermissions(permissions);
        ownerLink.setActive(true);
        ownerLink.setSingleUse(false);

        // Add the link to the share's links list
        share
            .getLinks()
            .add(ownerLink);
        shareRepository.persist(share);

        return share;
    }

    // Helper method to create test data for a share
    private void createTestDataForShare(Share share) {
        // Use the same owner format as getOwner would return for SharePrincipal
        String shareOwner = String.format("share: %s", share.getUuid());

        // Create a product group
        Product product1 = productRepository
            .findByIdOptional(Testdata.PRODUCT_1_UUID)
            .orElseThrow();
        ProductGroup group = new ProductGroup();
        group.setName("Test Group");
        group.setOwner(shareOwner);
        group.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.0"))));
        productGroupRepository.persist(group);

        // Create a shopping list
        ShoppingList list = new ShoppingList();
        list.setName("Test List");
        list.setOwner(shareOwner);
        list.setRepeating(false);
        list.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
        list.setProductGroups(new HashMap<>());
        shoppingListRepository.persist(list);

        // Create a shopping trip with required store
        Store store = storeRepository
            .findByIdOptional(Testdata.STORE_1_UUID)
            .orElseThrow();
        ShoppingTrip trip = new ShoppingTrip();
        trip.setStore(store);
        trip.setOwner(shareOwner);
        trip.setTime(java.time.Instant.now());
        trip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("3.0"))));
        shoppingTripRepository.persist(trip);
    }
}
