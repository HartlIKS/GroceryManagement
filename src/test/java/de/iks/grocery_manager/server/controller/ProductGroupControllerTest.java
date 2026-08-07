package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.locks.EntityAccess;
import de.iks.grocery_manager.server.model.ProductGroup;
import de.iks.grocery_manager.server.model.masterdata.Product;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
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
@TestHTTPEndpoint(ProductGroupController.class)
@WithTestUser
@EntityAccess(ProductGroupRepository.class)
class ProductGroupControllerTest {

    @Inject
    ProductGroupRepository productGroupRepository;

    @Inject
    ProductRepository productRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(ProductGroupController.class)
    @WithTestUser
    class GetProductGroup {
        @Test
        void shouldReturnProductGroupWhenFound() {
            QuarkusTransaction.begin();
            // Create fresh test data
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ProductGroup testGroup = new ProductGroup();
            testGroup.setName("Test Group 1");
            testGroup.setOwner(WithTestUser.OWNER);
            testGroup.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            productGroupRepository.persist(testGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .defaultParser(Parser.JSON)
                .body(
                    "uuid",
                    is(testGroup
                           .getUuid()
                           .toString())
                )
                .body("name", is("Test Group 1"))
                .body("products.%s", withArgs(Testdata.PRODUCT_1_UUID), is(2.5f))
                .body("products.%s", withArgs(Testdata.PRODUCT_2_UUID), is(1.0f))
                .when()
                .get("{uuid}", testGroup.getUuid());
        }

        @Test
        void shouldReturn404WhenProductGroupNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn404WhenAccessingProductGroupOfDifferentUser() {
            QuarkusTransaction.begin();

            // Create group for different user
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>());
            productGroupRepository.persist(otherUserGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", otherUserGroup.getUuid());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductGroupController.class)
    @WithTestUser
    class CreateProductGroup {
        @Test
        void shouldCreateProductGroupWhenAuthorized() {
            QuarkusTransaction.begin();

            long initialCount = productGroupRepository.count();

            // Create canary group first
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(canaryGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            String createJson = String.format(
                """
                    {
                      "name": "New Group",
                      "products": {
                        "%s": 1.5,
                        "%s": 2.0
                      }
                    }""", Testdata.PRODUCT_1_UUID, Testdata.PRODUCT_3_UUID
            );

            expect()
                .statusCode(201)
                .header("location", matchesRegex(
                    String.format("%s/%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern())
                ))
                .contentType(ContentType.JSON)
                .body("uuid", matchesRegex(Testdata.UUID_PATTERN))
                .body("name", is("New Group"))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_1_UUID), is(1.5f))
                .body("products.%s", withArgs(Testdata.PRODUCT_3_UUID), is(2.0f))
                .given()
                .contentType(ContentType.JSON)
                .body(createJson)
                .post();

            // Verify creation
            assertEquals(initialCount + 2, productGroupRepository.count());

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findByIdOptional(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductGroupController.class)
    @WithTestUser
    class UpdateProductGroup {
        @Test
        void shouldUpdateProductGroupWhenAuthorized() {
            QuarkusTransaction.begin();

            long initialCount = productGroupRepository.count();

            // Create test group
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ProductGroup testGroup = new ProductGroup();
            testGroup.setName("Test Group 1");
            testGroup.setOwner(WithTestUser.OWNER);
            testGroup.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            productGroupRepository.persist(testGroup);

            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(canaryGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = String.format(
                """
                    {
                      "name": "Updated Group 1",
                      "products": {
                        "%s": 4.0,
                        "%s": 2.5
                      }
                    }""", Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID
            );

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("name", is("Updated Group 1"))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_3_UUID), is(4.0f))
                .body("products.%s", withArgs(Testdata.PRODUCT_4_UUID), is(2.5f))
                .given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .put("{uuid}", testGroup.getUuid());

            // Verify update and other groups unaffected
            assertEquals(initialCount + 2, productGroupRepository.count());
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());

            ProductGroup unchangedCanary = productGroupRepository
                .findByIdOptional(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
        }

        @Test
        void shouldReturn404WhenUpdatingAnotherUsersProductGroup() {
            QuarkusTransaction.begin();

            // Create group for different user
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(otherUserGroup);

            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(canaryGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "name": "Unauthorized Update",
                  "products": {}
                }""";

            expect()
                .statusCode(404)
                .given()
                .contentType(ContentType.JSON)
                .body(updateJson)
                .put("{uuid}", otherUserGroup.getUuid());

            // Verify other user's group unchanged
            ProductGroup unchangedOtherGroup = productGroupRepository
                .findByIdOptional(otherUserGroup.getUuid())
                .orElseThrow();
            assertEquals("Other User Group", unchangedOtherGroup.getName());

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findByIdOptional(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductGroupController.class)
    @WithTestUser
    class DeleteProductGroup {
        @Test
        void shouldDeleteProductGroupWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test group
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ProductGroup testGroup = new ProductGroup();
            testGroup.setName("Test Group 1");
            testGroup.setOwner(WithTestUser.OWNER);
            testGroup.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            productGroupRepository.persist(testGroup);

            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(canaryGroup);

            productGroupRepository.flush();

            long initialCount = productGroupRepository.count();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", testGroup.getUuid());

            // Verify deletion
            assertEquals(initialCount - 1, productGroupRepository.count());
            assertFalse(productGroupRepository
                            .findByIdOptional(testGroup.getUuid())
                            .isPresent());

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn404WhenDeletingAnotherUsersProductGroup() {
            QuarkusTransaction.begin();

            // Create group for different user
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(otherUserGroup);

            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(canaryGroup);

            productGroupRepository.flush();

            long initialCount = productGroupRepository.count();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", otherUserGroup.getUuid());

            // Verify other user's group still exists and unchanged
            assertTrue(productGroupRepository
                           .findByIdOptional(otherUserGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedOtherGroup = productGroupRepository
                .findByIdOptional(otherUserGroup.getUuid())
                .orElseThrow();
            assertEquals("Other User Group", unchangedOtherGroup.getName());

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductGroupController.class)
    @WithTestUser
    class SearchProductGroups {
        @Test
        void shouldReturnAllProductGroupsWhenSearchingWithEmptyName() {
            QuarkusTransaction.begin();

            // Create test groups
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();
            Product product3 = productRepository
                .findByIdOptional(Testdata.PRODUCT_3_UUID)
                .orElseThrow();

            ProductGroup testGroup1 = new ProductGroup();
            testGroup1.setName("Test Group 1");
            testGroup1.setOwner(WithTestUser.OWNER);
            testGroup1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            productGroupRepository.persist(testGroup1);

            ProductGroup testGroup2 = new ProductGroup();
            testGroup2.setName("Test Group 2");
            testGroup2.setOwner(WithTestUser.OWNER);
            testGroup2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            productGroupRepository.persist(testGroup2);

            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            productGroupRepository.persist(canaryGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(3))
                .body("page.totalPages", is(1))
                .body("content.find { it.name == 'Test Group 1' }.uuid", isUuidOf(testGroup1))
                .body("content.find { it.name == 'Test Group 2' }.uuid", isUuidOf(testGroup2))
                .body("content.find { it.name == 'Canary Group' }.uuid", isUuidOf(canaryGroup))
                .when()
                .get();

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findByIdOptional(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }

        @Test
        void shouldReturnFilteredProductGroupsWhenSearchingWithName() {
            QuarkusTransaction.begin();

            // Create test groups
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();
            Product product3 = productRepository
                .findByIdOptional(Testdata.PRODUCT_3_UUID)
                .orElseThrow();

            ProductGroup testGroup1 = new ProductGroup();
            testGroup1.setName("Test Group 1");
            testGroup1.setOwner(WithTestUser.OWNER);
            testGroup1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            productGroupRepository.persist(testGroup1);

            ProductGroup testGroup2 = new ProductGroup();
            testGroup2.setName("Test Group 2");
            testGroup2.setOwner(WithTestUser.OWNER);
            testGroup2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            productGroupRepository.persist(testGroup2);

            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            productGroupRepository.persist(canaryGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1))
                .body("content.find { it.name == 'Test Group 1' }.uuid", isUuidOf(testGroup1))
                .body("content.find { it.name == 'Test Group 2' }.uuid", isUuidOf(testGroup2))
                .body("content.find { it.name == 'Canary Group' }", nullValue())
                .given()
                .queryParam("name", "Test")
                .get();

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findByIdOptional(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
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
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(0))
                .body("content.size()", is(0))
                .given()
                .queryParam("name", "NonExistent")
                .get();
        }

        @Test
        void shouldReturnOnlyUserOwnsProductGroups() {
            QuarkusTransaction.begin();

            // Create test groups for current user
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ProductGroup testGroup1 = new ProductGroup();
            testGroup1.setName("Test Group 1");
            testGroup1.setOwner(WithTestUser.OWNER);
            testGroup1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            productGroupRepository.persist(testGroup1);

            ProductGroup testGroup2 = new ProductGroup();
            testGroup2.setName("Test Group 2");
            testGroup2.setOwner(WithTestUser.OWNER);
            testGroup2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            productGroupRepository.persist(testGroup2);

            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner(WithTestUser.OWNER);
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.persist(canaryGroup);

            // Create group for different user
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Test Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>());
            productGroupRepository.persist(otherUserGroup);

            productGroupRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1))
                .body("content.find { it.name == 'Test Group 1' }.uuid", isUuidOf(testGroup1))
                .body("content.find { it.name == 'Test Group 2' }.uuid", isUuidOf(testGroup2))
                .body("content.find { it.name == 'Other User Test Group' }", nullValue())
                .given()
                .queryParam("name", "Test")
                .get();

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findByIdOptional(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findByIdOptional(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }
    }
}
