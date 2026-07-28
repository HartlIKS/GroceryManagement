package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.model.ShoppingList;
import de.iks.grocery_manager.server.model.masterdata.Product;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static de.iks.grocery_manager.server.UUIDMatcher.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(ShoppingListController.class)
@WithTestUser
@Sql("/testdata.sql")
class ShoppingListControllerTest {

    @Inject
    ShoppingListRepository shoppingListRepository;

    @Inject
    ProductRepository productRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(ShoppingListController.class)
    @WithTestUser
    class GetShoppingList {
        @Test
        void shouldReturnShoppingListWhenFound() {
            QuarkusTransaction.begin();
            // Create fresh test data
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ShoppingList testList = new ShoppingList();
            testList.setName("Test List 1");
            testList.setOwner(WithTestUser.OWNER);
            testList.setRepeating(false);
            testList.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(testList))
                .body("name", is("Test List 1"))
                .body("repeating", is(false))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_1_UUID), is(2.5f))
                .body("products.%s", withArgs(Testdata.PRODUCT_2_UUID), is(1f))
                .body("productGroups.size()", is(0))
                .when()
                .get("{uuid}", testList.getUuid());
        }

        @Test
        void shouldReturn404WhenShoppingListNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn404WhenAccessingShoppingListOfDifferentUser() {
            QuarkusTransaction.begin();

            // Create list for different user
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(otherUserList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", otherUserList.getUuid());
        }
    }

    @Nested
    @TestHTTPEndpoint(ShoppingListController.class)
    @WithTestUser
    class CreateShoppingList {
        @Test
        void shouldCreateShoppingListWhenAuthorized() {
            QuarkusTransaction.begin();

            long initialCount = shoppingListRepository.count();

            // Create canary list first
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            String createJson = String.format(
                """
                    {
                      "name": "New List",
                      "repeating": false,
                      "products": {
                        "%s": 1.5,
                        "%s": 2.0
                      },
                      "productGroups": {}
                    }""", Testdata.PRODUCT_1_UUID, Testdata.PRODUCT_3_UUID
            );

            expect()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .header(
                    "location",
                    matchesRegex(String.format("%s/%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern()))
                )
                .body("name", is("New List"))
                .body("repeating", is(false))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_1_UUID), is(1.5f))
                .body("products.%s", withArgs(Testdata.PRODUCT_3_UUID), is(2f))
                .body("productGroups.size()", is(0))
                .given()
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();

            // Verify creation
            assertEquals(initialCount + 2, shoppingListRepository.count());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }
    }

    @Nested
    @TestHTTPEndpoint(ShoppingListController.class)
    @WithTestUser
    class UpdateShoppingList {
        @Test
        void shouldUpdateShoppingListWhenAuthorized() {
            QuarkusTransaction.begin();

            long initialCount = shoppingListRepository.count();

            // Create test list
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ShoppingList testList = new ShoppingList();
            testList.setName("Test List 1");
            testList.setOwner(WithTestUser.OWNER);
            testList.setRepeating(false);
            testList.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList);

            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = String.format(
                """
                    {
                      "name": "Updated List 1",
                      "repeating": true,
                      "products": {
                        "%s": 4.0,
                        "%s": 2.5
                      },
                      "productGroups": {}
                    }""", Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID
            );

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(testList))
                .body("name", is("Updated List 1"))
                .body("repeating", is(true))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_3_UUID), is(4f))
                .body("products.%s", withArgs(Testdata.PRODUCT_4_UUID), is(2.5f))
                .body("productGroups.size()", is(0))
                .given()
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", testList.getUuid());

            // Verify update and other lists unaffected
            assertEquals(initialCount + 2, shoppingListRepository.count());
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());

            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
        }

        @Test
        void shouldReturn404WhenUpdatingAnotherUsersShoppingList() {
            QuarkusTransaction.begin();

            // Create list for different user
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(otherUserList);

            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Unauthorized Update",
                  "repeating": false,
                  "products": {},
                  "productGroups": {}
                }""";

            expect()
                .statusCode(404)
                .given()
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", otherUserList.getUuid());

            // Verify other user's list unchanged
            ShoppingList unchangedOtherList = shoppingListRepository
                .findByIdOptional(otherUserList.getUuid())
                .orElseThrow();
            assertEquals("Other User List", unchangedOtherList.getName());
            assertFalse(unchangedOtherList.isRepeating());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
        }
    }

    @Nested
    @TestHTTPEndpoint(ShoppingListController.class)
    @WithTestUser
    class DeleteShoppingList {
        @Test
        void shouldDeleteShoppingListWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test list
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ShoppingList testList = new ShoppingList();
            testList.setName("Test List 1");
            testList.setOwner(WithTestUser.OWNER);
            testList.setRepeating(false);
            testList.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList);

            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            long initialCount = shoppingListRepository.count();

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", testList.getUuid());

            // Verify deletion
            assertEquals(initialCount - 1, shoppingListRepository.count());
            assertFalse(shoppingListRepository
                            .findByIdOptional(testList.getUuid())
                            .isPresent());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn404WhenDeletingAnotherUsersShoppingList() {
            QuarkusTransaction.begin();

            // Create list for different user
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(otherUserList);

            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            long initialCount = shoppingListRepository.count();

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", otherUserList.getUuid());

            // Verify other user's list still exists and unchanged
            assertTrue(shoppingListRepository
                           .findByIdOptional(otherUserList.getUuid())
                           .isPresent());
            ShoppingList unchangedOtherList = shoppingListRepository
                .findByIdOptional(otherUserList.getUuid())
                .orElseThrow();
            assertEquals("Other User List", unchangedOtherList.getName());
            assertFalse(unchangedOtherList.isRepeating());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            assertEquals(initialCount, shoppingListRepository.count());
        }

        @Test
        void shouldDeleteNonRepeatingListWhenIfNonRepeatingIsTrue() {
            QuarkusTransaction.begin();

            // Create non-repeating test list
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingList nonRepeatingList = new ShoppingList();
            nonRepeatingList.setName("Non-Repeating List");
            nonRepeatingList.setOwner(WithTestUser.OWNER);
            nonRepeatingList.setRepeating(false);
            nonRepeatingList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            nonRepeatingList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(nonRepeatingList);

            // Create repeating canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Repeating Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            long initialCount = shoppingListRepository.count();

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .given()
                .queryParam("ifNonRepeating", true)
                .when()
                .delete("{uuid}", nonRepeatingList.getUuid());

            // Verify deletion of non-repeating list
            assertEquals(initialCount - 1, shoppingListRepository.count());
            assertFalse(shoppingListRepository
                            .findByIdOptional(nonRepeatingList.getUuid())
                            .isPresent());

            // Verify repeating canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Repeating Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
        }

        @Test
        void shouldNotDeleteRepeatingListWhenIfNonRepeatingIsTrue() {
            QuarkusTransaction.begin();

            // Create repeating test list
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingList repeatingList = new ShoppingList();
            repeatingList.setName("Repeating List");
            repeatingList.setOwner(WithTestUser.OWNER);
            repeatingList.setRepeating(true);
            repeatingList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            repeatingList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(repeatingList);

            // Create non-repeating canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Non-Repeating Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            long initialCount = shoppingListRepository.count();

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .given()
                .queryParam("ifNonRepeating", true)
                .when()
                .delete("{uuid}", repeatingList.getUuid());

            // Verify repeating list was NOT deleted
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository
                           .findByIdOptional(repeatingList.getUuid())
                           .isPresent());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Non-Repeating Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
        }
    }

    @Nested
    @TestHTTPEndpoint(ShoppingListController.class)
    @WithTestUser
    class SearchShoppingLists {
        @Test
        void shouldReturnAllShoppingListsWhenSearchingWithEmptyName() {
            QuarkusTransaction.begin();

            // Create test lists
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();
            Product product3 = productRepository
                .findByIdOptional(Testdata.PRODUCT_3_UUID)
                .orElseThrow();

            ShoppingList testList1 = new ShoppingList();
            testList1.setName("Test List 1");
            testList1.setOwner(WithTestUser.OWNER);
            testList1.setRepeating(false);
            testList1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            testList1.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList1);

            ShoppingList testList2 = new ShoppingList();
            testList2.setName("Test List 2");
            testList2.setOwner(WithTestUser.OWNER);
            testList2.setRepeating(true);
            testList2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            testList2.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList2);

            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(3))
                .body("page.totalPages", is(1))
                .body("content.find { it.name == 'Test List 1' }.uuid", isUuidOf(testList1))
                .body("content.find { it.name == 'Test List 2' }.uuid", isUuidOf(testList2))
                .body("content.find { it.name == 'Canary List' }.uuid", isUuidOf(canaryList))
                .when()
                .get();

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }

        @Test
        void shouldReturnFilteredShoppingListsWhenSearchingWithName() {
            QuarkusTransaction.begin();

            // Create test lists
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();
            Product product3 = productRepository
                .findByIdOptional(Testdata.PRODUCT_3_UUID)
                .orElseThrow();

            ShoppingList testList1 = new ShoppingList();
            testList1.setName("Test List 1");
            testList1.setOwner(WithTestUser.OWNER);
            testList1.setRepeating(false);
            testList1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            testList1.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList1);

            ShoppingList testList2 = new ShoppingList();
            testList2.setName("Test List 2");
            testList2.setOwner(WithTestUser.OWNER);
            testList2.setRepeating(true);
            testList2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            testList2.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList2);

            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1))
                .body("content.find { it.name == 'Test List 1' }.uuid", isUuidOf(testList1))
                .body("content.find { it.name == 'Test List 2' }.uuid", isUuidOf(testList2))
                .body("content.find { it.name == 'Canary List' }", nullValue())
                .given()
                .queryParam("name", "Test")
                .when()
                .get();

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }

        @Test
        void shouldReturnEmptyResultWhenSearchingWithNonExistentName() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.totalElements", is(0))
                .body("content.size()", is(0))
                .given()
                .queryParam("name", "NonExistent")
                .when()
                .get();
        }

        @Test
        void shouldReturnOnlyUserOwnsShoppingLists() {
            QuarkusTransaction.begin();

            // Create test lists for current user
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ShoppingList testList1 = new ShoppingList();
            testList1.setName("Test List 1");
            testList1.setOwner(WithTestUser.OWNER);
            testList1.setRepeating(false);
            testList1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            testList1.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList1);

            ShoppingList testList2 = new ShoppingList();
            testList2.setName("Test List 2");
            testList2.setOwner(WithTestUser.OWNER);
            testList2.setRepeating(true);
            testList2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            testList2.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(testList2);

            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner(WithTestUser.OWNER);
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(canaryList);

            // Create list for different user
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User Test List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>());
            otherUserList.setProductGroups(new HashMap<>());
            shoppingListRepository.persist(otherUserList);

            shoppingListRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.totalElements", is(2))
                .body("content.size()", is(2))
                .body("content.find { it.name == 'Test List 1' }.uuid", isUuidOf(testList1))
                .body("content.find { it.name == 'Test List 2' }.uuid", isUuidOf(testList2))
                .body("content.find { it.name == 'Other User Test List' }", nullValue())
                .given()
                .queryParam("name", "Test")
                .when()
                .get();

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findByIdOptional(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findByIdOptional(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }
    }
}
