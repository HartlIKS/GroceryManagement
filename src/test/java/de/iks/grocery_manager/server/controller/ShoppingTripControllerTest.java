package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@WithTestUser
@TestHTTPEndpoint(ShoppingTripController.class)
@Sql("/testdata.sql")
class ShoppingTripControllerTest {

    @Inject
    ShoppingTripRepository shoppingTripRepository;

    @Inject
    ProductRepository productRepository;

    @Inject
    StoreRepository storeRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @WithTestUser
    @TestHTTPEndpoint(ShoppingTripController.class)
    class GetShoppingTrip {
        @Test
        void shouldReturnShoppingTripWhenFound() {
            QuarkusTransaction.begin();

            // Create fresh test data
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner(WithTestUser.OWNER);
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            shoppingTripRepository.persist(testTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "uuid",
                    is(testTrip
                           .getUuid()
                           .toString())
                )
                .body("store", is(Testdata.STORE_1_UUID.toString()))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_1_UUID), is(2.5f))
                .body("products.%s", withArgs(Testdata.PRODUCT_2_UUID), is(1f))
                .when()
                .get("{uuid}", testTrip.getUuid());
        }

        @Test
        void shouldReturn404WhenShoppingTripNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn404WhenAccessingShoppingTripOfDifferentUser() {
            QuarkusTransaction.begin();

            // Create trip for different user
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(otherUserTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", otherUserTrip.getUuid());
        }
    }

    @Nested
    @WithTestUser
    @TestHTTPEndpoint(ShoppingTripController.class)
    class CreateShoppingTrip {
        @Test
        void shouldCreateShoppingTripWhenAuthorized() {
            QuarkusTransaction.begin();

            long initialCount = shoppingTripRepository.count();

            // Create canary trip first
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            shoppingTripRepository.flush();
            QuarkusTransaction.commit();

            Instant testTime = Instant.now();
            String createJson = String.format(
                """
                    {
                      "store": "%s",
                      "time": "%s",
                      "products": {
                        "%s": 1.5,
                        "%s": 2.0
                      }
                    }""",
                Testdata.STORE_2_UUID,
                testTime.toString(),
                Testdata.PRODUCT_1_UUID,
                Testdata.PRODUCT_3_UUID
            );

            expect()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .header(
                    "location",
                    matchesRegex(String.format("%s/%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern()))
                )
                .body("store", is(Testdata.STORE_2_UUID.toString()))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_1_UUID), is(1.5f))
                .body("products.%s", withArgs(Testdata.PRODUCT_3_UUID), is(2f))
                .given()
                .body(createJson)
                .contentType(ContentType.JSON)
                .when()
                .post();

            // Verify creation
            assertEquals(initialCount + 2, shoppingTripRepository.count());

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedCanary
                    .getStore()
                    .getUuid()
            );
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }
    }

    @Nested
    @WithTestUser
    @TestHTTPEndpoint(ShoppingTripController.class)
    class UpdateShoppingTrip {
        @Test
        void shouldUpdateShoppingTripWhenAuthorized() {
            QuarkusTransaction.begin();

            long initialCount = shoppingTripRepository.count();

            // Create test trip
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner(WithTestUser.OWNER);
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            shoppingTripRepository.persist(testTrip);

            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            Instant updatedTime = Instant
                .now()
                .plusSeconds(3600);
            String updateJson = String.format(
                """
                    {
                      "store": "%s",
                      "time": "%s",
                      "products": {
                        "%s": 4.0,
                        "%s": 2.5
                      }
                    }""",
                Testdata.STORE_3_UUID,
                updatedTime.toString(),
                Testdata.PRODUCT_3_UUID,
                Testdata.PRODUCT_4_UUID
            );

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "uuid",
                    is(testTrip
                           .getUuid()
                           .toString())
                )
                .body("store", is(Testdata.STORE_3_UUID.toString()))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_3_UUID), is(4f))
                .body("products.%s", withArgs(Testdata.PRODUCT_4_UUID), is(2.5f))
                .given()
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", testTrip.getUuid());

            // Verify update and other trips unaffected
            assertEquals(initialCount + 2, shoppingTripRepository.count());
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());

            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedCanary
                    .getStore()
                    .getUuid()
            );
        }

        @Test
        void shouldReturn404WhenUpdatingAnotherUsersShoppingTrip() {
            QuarkusTransaction.begin();

            // Create trip for different user
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(otherUserTrip);

            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            String updateJson = """
                {
                  "store": "00000000-0000-0000-0000-000000000003",
                  "time": "2026-03-05T10:00:00Z",
                  "products": {}
                }""";

            expect()
                .statusCode(404)
                .given()
                .body(updateJson)
                .contentType(ContentType.JSON)
                .when()
                .put("{uuid}", otherUserTrip.getUuid());

            // Verify other user's trip unchanged
            ShoppingTrip unchangedOtherTrip = shoppingTripRepository
                .findByIdOptional(otherUserTrip.getUuid())
                .orElseThrow();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedOtherTrip
                    .getStore()
                    .getUuid()
            );

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedCanary
                    .getStore()
                    .getUuid()
            );
        }
    }

    @Nested
    @WithTestUser
    @TestHTTPEndpoint(ShoppingTripController.class)
    class AddToShoppingTrip {
        @Test
        void shouldAddProductsToShoppingTripWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test trip
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();

            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner(WithTestUser.OWNER);
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5")
            )));
            shoppingTripRepository.persist(testTrip);

            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            String addJson = String.format(
                """
                    {
                      "%s": 1.5,
                      "%s": 2.0
                    }""",
                Testdata.PRODUCT_1_UUID,
                Testdata.PRODUCT_3_UUID
            );

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "uuid",
                    is(testTrip
                           .getUuid()
                           .toString())
                )
                .body("store", is(Testdata.STORE_1_UUID.toString()))
                .body("products.size()", is(2))
                .body("products.%s", withArgs(Testdata.PRODUCT_1_UUID), is(4f))
                .body("products.%s", withArgs(Testdata.PRODUCT_3_UUID), is(2f))
                .given()
                .body(addJson)
                .contentType(ContentType.JSON)
                .when()
                .post("{uuid}/add", testTrip.getUuid());

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
            assertEquals(
                new BigDecimal("1.0"),
                unchangedCanary
                    .getProducts()
                    .get(product1)
            );
        }

        @Test
        void shouldReturn404WhenAddingToAnotherUsersShoppingTrip() {
            QuarkusTransaction.begin();

            // Create trip for different user
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(otherUserTrip);

            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            String addJson = String.format(
                """
                    {
                      "%s": 2.0
                    }""",
                Testdata.PRODUCT_2_UUID
            );

            expect()
                .statusCode(404)
                .given()
                .body(addJson)
                .contentType(ContentType.JSON)
                .when()
                .post("{uuid}/add", otherUserTrip.getUuid());

            // Verify other user's trip unchanged
            ShoppingTrip unchangedOtherTrip = shoppingTripRepository
                .findByIdOptional(otherUserTrip.getUuid())
                .orElseThrow();
            assertEquals(
                1,
                unchangedOtherTrip
                    .getProducts()
                    .size()
            );

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }
    }

    @Nested
    @WithTestUser
    @TestHTTPEndpoint(ShoppingTripController.class)
    class DeleteShoppingTrip {
        @Test
        void shouldDeleteShoppingTripWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test trip
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner(WithTestUser.OWNER);
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            shoppingTripRepository.persist(testTrip);

            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            long initialCount = shoppingTripRepository.count();

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", testTrip.getUuid());

            // Verify deletion
            assertEquals(initialCount - 1, shoppingTripRepository.count());
            assertFalse(shoppingTripRepository
                            .findByIdOptional(testTrip.getUuid())
                            .isPresent());

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
        }

        @Test
        void shouldReturn404WhenDeletingAnotherUsersShoppingTrip() {
            QuarkusTransaction.begin();

            // Create trip for different user
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(otherUserTrip);

            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            long initialCount = shoppingTripRepository.count();

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", otherUserTrip.getUuid());

            // Verify other user's trip still exists and unchanged
            assertTrue(shoppingTripRepository
                           .findByIdOptional(otherUserTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedOtherTrip = shoppingTripRepository
                .findByIdOptional(otherUserTrip.getUuid())
                .orElseThrow();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedOtherTrip
                    .getStore()
                    .getUuid()
            );

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            assertEquals(initialCount, shoppingTripRepository.count());
        }
    }

    @Nested
    @WithTestUser
    @TestHTTPEndpoint(ShoppingTripController.class)
    class SearchShoppingTrips {
        @Test
        void shouldReturnShoppingTripsInDateRangeWhenNoParametersProvided() {
            QuarkusTransaction.begin();

            // Create test trips with fixed timestamps
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Store store2 = storeRepository
                .findByIdOptional(Testdata.STORE_2_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();
            Product product3 = productRepository
                .findByIdOptional(Testdata.PRODUCT_3_UUID)
                .orElseThrow();

            // Use a fixed base time in the future
            Instant baseTime = Instant
                .now()
                .plus(1, ChronoUnit.DAYS);

            ShoppingTrip testTrip1 = new ShoppingTrip();
            testTrip1.setStore(store1);
            testTrip1.setOwner(WithTestUser.OWNER);
            testTrip1.setTime(baseTime.plus(Duration.ofMinutes(5)));
            testTrip1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            shoppingTripRepository.persist(testTrip1);

            ShoppingTrip testTrip2 = new ShoppingTrip();
            testTrip2.setStore(store2);
            testTrip2.setOwner(WithTestUser.OWNER);
            testTrip2.setTime(baseTime.plus(Duration.ofMinutes(10)));
            testTrip2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(testTrip2);

            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(baseTime.plus(Duration.ofMinutes(15)));
            canaryTrip.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            shoppingTripRepository.persist(canaryTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(3))
                .body("page.totalPages", is(1))
                .body("content.size()", is(3))
                .body("content.find { it.uuid == '%s' }", withArgs(testTrip1.getUuid().toString()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(testTrip2.getUuid().toString()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(canaryTrip.getUuid().toString()), notNullValue())
                .when()
                .get();

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedCanary
                    .getStore()
                    .getUuid()
            );
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }

        @Test
        void shouldReturnFilteredShoppingTripsWhenSearchingWithDateRange() {
            QuarkusTransaction.begin();

            // Create test trips with fixed timestamps
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Store store2 = storeRepository
                .findByIdOptional(Testdata.STORE_2_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();
            Product product3 = productRepository
                .findByIdOptional(Testdata.PRODUCT_3_UUID)
                .orElseThrow();

            // Use a fixed base time
            Instant baseTime = Instant.parse("2026-03-05T15:00:00Z");

            ShoppingTrip testTrip1 = new ShoppingTrip();
            testTrip1.setStore(store1);
            testTrip1.setOwner(WithTestUser.OWNER);
            testTrip1.setTime(baseTime.plus(Duration.ofMinutes(5))); // 15:05:00
            testTrip1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            shoppingTripRepository.persist(testTrip1);

            ShoppingTrip testTrip2 = new ShoppingTrip();
            testTrip2.setStore(store2);
            testTrip2.setOwner(WithTestUser.OWNER);
            testTrip2.setTime(baseTime.plus(Duration.ofMinutes(10))); // 15:10:00
            testTrip2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(testTrip2);

            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(baseTime.plus(Duration.ofHours(24))); // Next day
            canaryTrip.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            shoppingTripRepository.persist(canaryTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1))
                .body("content.size()", is(2))
                .body("content.find { it.uuid == '%s' }", withArgs(testTrip1.getUuid().toString()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(testTrip2.getUuid().toString()), notNullValue())
                .given()
                .queryParam("from", "2026-03-05T15:04:00Z")
                .queryParam("to", "2026-03-05T15:12:00Z")
                .when()
                .get();

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedCanary
                    .getStore()
                    .getUuid()
            );
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }

        @Test
        void shouldReturnEmptyResultWhenSearchingWithNonOverlappingDateRange() {
            ZonedDateTime futureFrom = ZonedDateTime
                .now()
                .plusDays(30);
            ZonedDateTime futureTo = futureFrom.plusWeeks(1);

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.totalElements", is(0))
                .body("content.size()", is(0))
                .given()
                .queryParam("from", futureFrom.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                .queryParam("to", futureTo.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                .when()
                .get();
        }

        @Test
        void shouldReturnOnlyUserOwnsShoppingTrips() {
            QuarkusTransaction.begin();

            // Create test trips for current user with fixed timestamps
            Store store1 = storeRepository
                .findByIdOptional(Testdata.STORE_1_UUID)
                .orElseThrow();
            Store store2 = storeRepository
                .findByIdOptional(Testdata.STORE_2_UUID)
                .orElseThrow();
            Product product1 = productRepository
                .findByIdOptional(Testdata.PRODUCT_1_UUID)
                .orElseThrow();
            Product product2 = productRepository
                .findByIdOptional(Testdata.PRODUCT_2_UUID)
                .orElseThrow();

            // Use a fixed base time
            Instant baseTime = Instant.parse("2026-03-05T15:00:00Z");

            ShoppingTrip testTrip1 = new ShoppingTrip();
            testTrip1.setStore(store1);
            testTrip1.setOwner(WithTestUser.OWNER);
            testTrip1.setTime(baseTime.plus(Duration.ofMinutes(5))); // 15:05:00
            testTrip1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            shoppingTripRepository.persist(testTrip1);

            ShoppingTrip testTrip2 = new ShoppingTrip();
            testTrip2.setStore(store2);
            testTrip2.setOwner(WithTestUser.OWNER);
            testTrip2.setTime(baseTime.plus(Duration.ofMinutes(10))); // 15:10:00
            testTrip2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(testTrip2);

            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner(WithTestUser.OWNER);
            canaryTrip.setTime(baseTime.plus(Duration.ofMinutes(15))); // 15:15:00
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.persist(canaryTrip);

            // Create trip for different user
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(baseTime.plus(Duration.ofMinutes(5))); // Same time as testTrip1
            otherUserTrip.setProducts(new HashMap<>());
            shoppingTripRepository.persist(otherUserTrip);

            shoppingTripRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.totalElements", is(3))
                .body("content.size()", is(3))
                .body("content.find { it.uuid == '%s' }", withArgs(testTrip1.getUuid().toString()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(testTrip2.getUuid().toString()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(canaryTrip.getUuid().toString()), notNullValue())
                .given()
                .queryParam("from", "2026-03-05T15:04:00Z")
                .queryParam("to", "2026-03-05T15:20:00Z")
                .when()
                .get();

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findByIdOptional(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findByIdOptional(canaryTrip.getUuid())
                .get();
            assertEquals(
                Testdata.STORE_1_UUID,
                unchangedCanary
                    .getStore()
                    .getUuid()
            );
            assertEquals(
                1,
                unchangedCanary
                    .getProducts()
                    .size()
            );
        }
    }
}
