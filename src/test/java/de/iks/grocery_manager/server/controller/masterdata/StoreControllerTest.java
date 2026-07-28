package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static de.iks.grocery_manager.server.UUIDMatcher.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(StoreController.class)
@Sql("/testdata.sql")
class StoreControllerTest {
    @Inject
    StoreRepository storeRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(StoreController.class)
    @WithTestUser
    class GetStore {
        @Test
        void shouldReturnStoreWhenFound() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuid(Testdata.STORE_1_UUID))
                .body("name", is("Store 1"))
                .body("address.country", is("DE"))
                .body("address.city", is("Düsseldorf"))
                .body("currency", is("EUR"))
                .when()
                .get("{uuid}", Testdata.STORE_1_UUID);
        }

        @Test
        void shouldReturn404WhenStoreNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreController.class)
    class UpdateStore {
        public static final String STORE_1_UPDATE_JSON = """
            {
              "name": "Store 1b",
              "address": {
                "city": "Hilden"
              }
            }""";

        @Test
        @WithAdminUser
        void shouldUpdateStoreWhenAuthorizedAndFound() {
            long initialCount = storeRepository.count();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuid(Testdata.STORE_1_UUID))
                .body("name", is("Store 1b"))
                .body("address.country", is("DE"))
                .body("address.city", is("Hilden"))
                .body("currency", is("EUR"))
                .given()
                .contentType(ContentType.JSON)
                .body(STORE_1_UPDATE_JSON)
                .put("{uuid}", Testdata.STORE_1_UUID);

            // Verify update was applied and other store unaffected
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_1_UUID)
                           .isPresent());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_2_UUID)
                           .isPresent());
            assertEquals(initialCount, storeRepository.count());
        }

        @Test
        @WithAdminUser
        void shouldReturn404WhenUpdatingNonExistentStore() {
            long initialCount = storeRepository.count();

            expect()
                .statusCode(404)
                .given()
                .contentType(ContentType.JSON)
                .body(STORE_1_UPDATE_JSON)
                .put("{uuid}", Testdata.BAD_UUID);

            // Verify no changes to existing stores
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_1_UUID)
                           .isPresent());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_2_UUID)
                           .isPresent());
            assertEquals(initialCount, storeRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenUpdatingStoreWithoutAuthorization() {
            long initialCount = storeRepository.count();

            expect()
                .statusCode(403)
                .given()
                .contentType(ContentType.JSON)
                .body(STORE_1_UPDATE_JSON)
                .put("{uuid}", Testdata.STORE_1_UUID);

            // Verify no changes when unauthorized
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_1_UUID)
                           .isPresent());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_2_UUID)
                           .isPresent());
            assertEquals(initialCount, storeRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreController.class)
    class CreateStore {
        public static final String STORE_3_CREATE_JSON = """
            {
              "name": "Store 3",
              "address": {
                "country": "DE",
                "city": "Munich"
              },
              "currency": "EUR"
            }""";

        @Test
        @WithAdminUser
        void shouldCreateStoreWhenAuthorized() {
            long initialCount = storeRepository.count();

            expect()
                .statusCode(201)
                .header(
                    "location", matchesRegex(
                        String.format("%s/%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern())
                    )
                )
                .contentType(ContentType.JSON)
                .body("uuid", matchesRegex(Testdata.UUID_PATTERN))
                .body("name", is("Store 3"))
                .body("address.country", is("DE"))
                .body("address.city", is("Munich"))
                .body("currency", is("EUR"))
                .given()
                .contentType(ContentType.JSON)
                .body(STORE_3_CREATE_JSON)
                .post();

            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, storeRepository.count());
            // Verify existing stores unaffected
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_1_UUID)
                           .isPresent());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_2_UUID)
                           .isPresent());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenCreatingStoreWithoutAuthorization() {
            long initialCount = storeRepository.count();

            expect()
                .statusCode(403)
                .given()
                .contentType(ContentType.JSON)
                .body(STORE_3_CREATE_JSON)
                .post();

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeRepository.count());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_1_UUID)
                           .isPresent());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_2_UUID)
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreController.class)
    class DeleteStore {
        @Test
        @WithAdminUser
        void shouldDeleteStoreWhenAuthorized() {
            long initialCount = storeRepository.count();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", Testdata.STORE_1_UUID);

            // Verify deletion
            assertFalse(storeRepository
                            .findByIdOptional(Testdata.STORE_1_UUID)
                            .isPresent());
            assertEquals(initialCount - 1, storeRepository.count());
            // Verify other store unaffected
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_2_UUID)
                           .isPresent());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenDeletingStoreWithoutAuthorization() {
            long initialCount = storeRepository.count();

            expect()
                .statusCode(403)
                .when()
                .delete("{uuid}", Testdata.STORE_1_UUID);

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeRepository.count());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_1_UUID)
                           .isPresent());
            assertTrue(storeRepository
                           .findByIdOptional(Testdata.STORE_2_UUID)
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreController.class)
    @WithTestUser
    class SearchStores {
        @Test
        void shouldReturnStoresWhenSearchingByName() {
            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(4))
                .body("page.totalPages", is(1))
                .body("content[0].uuid", isUuid(Testdata.STORE_1_UUID))
                .body("content[0].name", is("Store 1"))
                .body("content[0].address.country", is("DE"))
                .body("content[0].address.city", is("Düsseldorf"))
                .body("content[0].currency", is("EUR"))
                .body("content[1].uuid", isUuid(Testdata.STORE_2_UUID))
                .body("content[1].name", is("Store 2"))
                .body("content[1].address.country", is("DE"))
                .body("content[1].address.city", is("Hilden"))
                .body("content[1].currency", is("USD"))
                .body("content[2].uuid", isUuid(Testdata.STORE_3_UUID))
                .body("content[2].name", is("Store 3"))
                .body("content[2].address.country", is("DE"))
                .body("content[2].address.city", is("Munich"))
                .body("content[2].currency", is("EUR"))
                .body("content[3].uuid", isUuid(Testdata.STORE_4_UUID))
                .body("content[3].name", is("Store 4"))
                .body("content[3].address.country", is("DE"))
                .body("content[3].address.city", is("Berlin"))
                .body("content[3].currency", is("USD"))
                .when()
                .get();
        }
    }
}