package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.masterdata.PriceRepository;
import de.iks.grocery_manager.server.locks.EntityAccess;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.regex.Pattern;

import static de.iks.grocery_manager.server.UUIDMatcher.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PriceListController.class)
@EntityAccess(PriceRepository.class)
class PriceListControllerTest {
    @Inject
    PriceRepository priceRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(PriceListController.class)
    @WithTestUser
    @EntityAccess(value = PriceRepository.class, writes = false)
    class GetPrice {
        @Test
        void shouldReturnPriceWhenFound() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuid(Testdata.PRICE_1_UUID))
                .body("store", is(Testdata.STORE_3_UUID.toString()))
                .body("product", is(Testdata.PRODUCT_3_UUID.toString()))
                .body("validFrom", is("2024-01-01T00:00:00Z"))
                .body("validTo", is("2024-12-31T23:59:59Z"))
                .body("price", is(10.99f))
                .when()
                .get("{uuid}", Testdata.PRICE_1_UUID);
        }

        @Test
        void shouldReturn404WhenPriceNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceListController.class)
    @WithAdminUser
    class UpdatePrice {
        private static final String PRICE_1_UPDATE_JSON = """
            {
              "validFrom": "2024-02-01T00:00:00Z",
              "validTo": "2024-11-30T23:59:59Z",
              "price": 12.99
            }""";

        @Test
        void shouldUpdatePriceWhenAuthorizedAndFound() {
            long initialCount = priceRepository.count();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuid(Testdata.PRICE_1_UUID))
                .body("store", is(Testdata.STORE_3_UUID.toString()))
                .body("product", is(Testdata.PRODUCT_3_UUID.toString()))
                .body("validFrom", is("2024-02-01T00:00:00Z"))
                .body("validTo", is("2024-11-30T23:59:59Z"))
                .body("price", is(12.99f))
                .given()
                .contentType(ContentType.JSON)
                .body(PRICE_1_UPDATE_JSON)
                .put("{uuid}", Testdata.PRICE_1_UUID);

            // Verify update was applied and other price unaffected
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_1_UUID)
                           .isPresent());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_2_UUID)
                           .isPresent());
            assertEquals(initialCount, priceRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentPrice() {
            long initialCount = priceRepository.count();

            expect()
                .statusCode(404)
                .given()
                .contentType(ContentType.JSON)
                .body(PRICE_1_UPDATE_JSON)
                .put("{uuid}", Testdata.BAD_UUID);

            // Verify no changes to existing prices
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_1_UUID)
                           .isPresent());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_2_UUID)
                           .isPresent());
            assertEquals(initialCount, priceRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenUpdatingPriceWithoutAuthorization() {
            long initialCount = priceRepository.count();

            expect()
                .statusCode(403)
                .given()
                .contentType(ContentType.JSON)
                .body(PRICE_1_UPDATE_JSON)
                .put("{uuid}", Testdata.PRICE_1_UUID);

            // Verify no changes when unauthorized
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_1_UUID)
                           .isPresent());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_2_UUID)
                           .isPresent());
            assertEquals(initialCount, priceRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceListController.class)
    class CreatePrice {
        private static final String PRICE_3_CREATE_JSON = String.format(
            """
                {
                  "store": "%s",
                  "product": "%s",
                  "validFrom": "2024-03-01T00:00:00Z",
                  "validTo": "2024-12-31T23:59:59Z",
                  "price": 8.99
                }""", Testdata.STORE_1_UUID, Testdata.PRODUCT_4_UUID
        );

        @Test
        @WithAdminUser
        void shouldCreatePriceWhenAuthorized() {
            long initialCount = priceRepository.count();

            expect()
                .statusCode(201)
                .header(
                    "location",
                    matchesRegex(String.format("%s/%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern()))
                )
                .contentType(ContentType.JSON)
                .body("uuid", matchesRegex(Testdata.UUID_PATTERN))
                .body("store", is(Testdata.STORE_1_UUID.toString()))
                .body("product", is(Testdata.PRODUCT_4_UUID.toString()))
                .body("validFrom", is("2024-03-01T00:00:00Z"))
                .body("validTo", is("2024-12-31T23:59:59Z"))
                .body("price", is(8.99f))
                .given()
                .contentType(ContentType.JSON)
                .body(PRICE_3_CREATE_JSON)
                .post();

            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, priceRepository.count());
            // Verify existing prices unaffected
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_1_UUID)
                           .isPresent());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_2_UUID)
                           .isPresent());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenCreatingPriceWithoutAuthorization() {
            long initialCount = priceRepository.count();

            expect()
                .statusCode(403)
                .given()
                .contentType(ContentType.JSON)
                .body(PRICE_3_CREATE_JSON)
                .post();

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceRepository.count());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_1_UUID)
                           .isPresent());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_2_UUID)
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceListController.class)
    class DeletePrice {
        @Test
        @WithAdminUser
        void shouldDeletePriceWhenAuthorized() {
            long initialCount = priceRepository.count();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", Testdata.PRICE_1_UUID);

            // Verify deletion
            assertFalse(priceRepository
                            .findByIdOptional(Testdata.PRICE_1_UUID)
                            .isPresent());
            assertEquals(initialCount - 1, priceRepository.count());
            // Verify other price unaffected
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_2_UUID)
                           .isPresent());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenDeletingPriceWithoutAuthorization() {
            long initialCount = priceRepository.count();

            expect()
                .statusCode(403)
                .when()
                .delete("{uuid}", Testdata.PRICE_1_UUID);

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceRepository.count());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_1_UUID)
                           .isPresent());
            assertTrue(priceRepository
                           .findByIdOptional(Testdata.PRICE_2_UUID)
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceListController.class)
    @WithTestUser
    @EntityAccess(value = PriceRepository.class, writes = false)
    class SearchPrices {
        @Test
        void shouldReturnAllPricesWhenSearching() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1))
                .body("content[0].uuid", isUuid(Testdata.PRICE_1_UUID))
                .body("content[0].store", is(Testdata.STORE_3_UUID.toString()))
                .body("content[0].product", is(Testdata.PRODUCT_3_UUID.toString()))
                .body("content[0].validFrom", is("2024-01-01T00:00:00Z"))
                .body("content[0].validTo", is("2024-12-31T23:59:59Z"))
                .body("content[0].price", is(10.99f))
                .body("content[1].uuid", isUuid(Testdata.PRICE_2_UUID))
                .body("content[1].store", is(Testdata.STORE_4_UUID.toString()))
                .body("content[1].product", is(Testdata.PRODUCT_4_UUID.toString()))
                .body("content[1].validFrom", is("2024-01-01T00:00:00Z"))
                .body("content[1].validTo", is("2024-12-31T23:59:59Z"))
                .body("content[1].price", is(5.49f))
                .when()
                .get();
        }

        @Test
        void shouldReturnPricesWhenSearchingByStore() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(1))
                .body("page.totalPages", is(1))
                .body("content[0].uuid", isUuid(Testdata.PRICE_1_UUID))
                .body("content[0].store", is(Testdata.STORE_3_UUID.toString()))
                .body("content[0].product", is(Testdata.PRODUCT_3_UUID.toString()))
                .body("content[0].validFrom", is("2024-01-01T00:00:00Z"))
                .body("content[0].validTo", is("2024-12-31T23:59:59Z"))
                .body("content[0].price", is(10.99f))
                .given()
                .queryParam("store", Testdata.STORE_3_UUID)
                .get();
        }

        @Test
        void shouldReturnPricesWhenSearchingByProduct() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(1))
                .body("page.totalPages", is(1))
                .body("content[0].uuid", isUuid(Testdata.PRICE_1_UUID))
                .body("content[0].store", is(Testdata.STORE_3_UUID.toString()))
                .body("content[0].product", is(Testdata.PRODUCT_3_UUID.toString()))
                .body("content[0].validFrom", is("2024-01-01T00:00:00Z"))
                .body("content[0].validTo", is("2024-12-31T23:59:59Z"))
                .body("content[0].price", is(10.99f))
                .given()
                .queryParam("product", Testdata.PRODUCT_3_UUID)
                .get();
        }

        @Test
        void shouldReturnPricesWhenSearchingByStoreAndProduct() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(1))
                .body("page.totalPages", is(1))
                .body("content[0].uuid", isUuid(Testdata.PRICE_1_UUID))
                .body("content[0].store", is(Testdata.STORE_3_UUID.toString()))
                .body("content[0].product", is(Testdata.PRODUCT_3_UUID.toString()))
                .body("content[0].validFrom", is("2024-01-01T00:00:00Z"))
                .body("content[0].validTo", is("2024-12-31T23:59:59Z"))
                .body("content[0].price", is(10.99f))
                .given()
                .queryParam("store", Testdata.STORE_3_UUID)
                .queryParam("product", Testdata.PRODUCT_3_UUID)
                .get();
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceListController.class)
    @WithTestUser
    @EntityAccess(value = PriceRepository.class, writes = false)
    class SearchPricesWithDateStoresAndProducts {
        @Test
        void shouldReturnPricesWhenSearchingWithValidDateStoresAndProducts() {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "%s.%s[0].listPriceUUID",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(Testdata.PRICE_1_UUID.toString())
                )
                .body(
                    "%s.%s[0].price",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(10.99f)
                )
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.STORE_3_UUID)
                .queryParam("products", Testdata.PRODUCT_3_UUID)
                .get();
        }

        @Test
        void shouldReturnMultiplePricesWhenSearchingWithMultipleStoresAndProducts() {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "%s.%s[0].listPriceUUID",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(Testdata.PRICE_1_UUID.toString())
                )
                .body(
                    "%s.%s[0].price",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(10.99f)
                )
                .body(
                    "%s.%s[0].listPriceUUID",
                    withArgs(Testdata.PRODUCT_4_UUID, Testdata.STORE_4_UUID),
                    is(Testdata.PRICE_2_UUID.toString())
                )
                .body(
                    "%s.%s[0].price",
                    withArgs(Testdata.PRODUCT_4_UUID, Testdata.STORE_4_UUID),
                    is(5.49f)
                )
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.STORE_3_UUID, Testdata.STORE_4_UUID)
                .queryParam("products", Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID)
                .get();
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithDateOutsideValidRange() {
            Instant searchDate = Instant.parse("2023-06-15T10:00:00Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("{}"))
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.STORE_3_UUID)
                .queryParam("products", Testdata.PRODUCT_3_UUID)
                .get();
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithNonExistentStore() {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("{}"))
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.BAD_UUID)
                .queryParam("products", Testdata.PRODUCT_3_UUID)
                .get();
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithNonExistentProduct() {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("{}"))
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.STORE_3_UUID)
                .queryParam("products", Testdata.BAD_UUID)
                .get();
        }

        @Test
        void shouldReturnPricesWhenSearchingWithDateAtValidFromBoundary() {
            Instant searchDate = Instant.parse("2024-01-01T00:00:00Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "%s.%s[0].listPriceUUID",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(Testdata.PRICE_1_UUID.toString())
                )
                .body(
                    "%s.%s[0].price",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(10.99f)
                )
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.STORE_3_UUID)
                .queryParam("products", Testdata.PRODUCT_3_UUID)
                .get();
        }

        @Test
        void shouldReturnPricesWhenSearchingWithDateAtValidToBoundary() {
            Instant searchDate = Instant.parse("2024-12-31T23:59:59Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(
                    "%s.%s[0].listPriceUUID",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(Testdata.PRICE_1_UUID.toString())
                )
                .body(
                    "%s.%s[0].price",
                    withArgs(Testdata.PRODUCT_3_UUID, Testdata.STORE_3_UUID),
                    is(10.99f)
                )
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.STORE_3_UUID)
                .queryParam("products", Testdata.PRODUCT_3_UUID)
                .get();
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithDateJustAfterValidTo() {
            Instant searchDate = Instant.parse("2025-01-01T00:00:00Z");

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("{}"))
                .given()
                .queryParam("at", searchDate.toString())
                .queryParam("stores", Testdata.STORE_3_UUID)
                .queryParam("products", Testdata.PRODUCT_3_UUID)
                .get();
        }
    }
}
