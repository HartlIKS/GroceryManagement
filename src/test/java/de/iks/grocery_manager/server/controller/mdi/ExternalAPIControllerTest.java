package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.controller.masterdata.WithAdminUser;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.locks.EntityAccess;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static de.iks.grocery_manager.server.UUIDMatcher.*;
import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(ExternalAPIController.class)
@WithAdminUser
@EntityAccess(ExternalAPIRepository.class)
class ExternalAPIControllerTest {

    @Inject
    ExternalAPIRepository externalAPIRepository;

    @TestHTTPResource
    String baseURI;

    @Nested
    @TestHTTPEndpoint(ExternalAPIController.class)
    @WithAdminUser
    class GetExternalAPI {
        @Test
        void shouldReturnExternalAPIWhenFound() {
            QuarkusTransaction.begin();

            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api);

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(api))
                .body("name", is("Test API"))
                .when()
                .get("{uuid}", api.getUuid());
        }

        @Test
        void shouldReturn404WhenExternalAPINotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(ExternalAPIController.class)
    @WithAdminUser
    class UpdateExternalAPI {
        private static final String EXTERNAL_API_1_UPDATE_JSON = """
        {
          "name": "External API 1 Updated"
        }""";

        @Test
        void shouldUpdateExternalAPIWhenAuthorizedAndFound() {
            QuarkusTransaction.begin();

            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api);

            long initialCount = externalAPIRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(api))
                .body("name", is("External API 1 Updated"))
                .given()
                .body(EXTERNAL_API_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", api.getUuid());

            // Verify update was applied
            assertTrue(externalAPIRepository
                           .findByIdOptional(api.getUuid())
                           .isPresent());
            assertEquals(
                "External API 1 Updated",
                externalAPIRepository
                    .findByIdOptional(api.getUuid())
                    .get()
                    .getName()
            );
            assertEquals(initialCount, externalAPIRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentExternalAPI() {
            long initialCount = externalAPIRepository.count();

            expect()
                .statusCode(404)
                .given()
                .body(EXTERNAL_API_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", Testdata.BAD_UUID);

            // Verify no changes
            assertEquals(initialCount, externalAPIRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenUpdatingExternalAPIWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api);

            long initialCount = externalAPIRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .body(EXTERNAL_API_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", api.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, externalAPIRepository.count());
            assertEquals(
                "Test API",
                externalAPIRepository
                    .findByIdOptional(api.getUuid())
                    .orElseThrow()
                    .getName()
            );
        }
    }

    @Nested
    @TestHTTPEndpoint(ExternalAPIController.class)
    @WithAdminUser
    class CreateExternalAPI {
        private static final String EXTERNAL_API_1_CREATE_JSON = """
        {
          "name": "External API 1"
        }""";

        @Test
        void shouldCreateExternalAPIWhenAuthorized() {
            long initialCount = externalAPIRepository.count();

            expect()
                .statusCode(201)
                .header(
                    "location",
                    matchesRegex(String.format("%s/%s", Pattern.quote(baseURI), Testdata.UUID_PATTERN.pattern()))
                )
                .contentType(ContentType.JSON)
                .body("name", is("External API 1"))
                .given()
                .body(EXTERNAL_API_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post();

            // Verify creation
            assertEquals(initialCount + 1, externalAPIRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenCreatingExternalAPIWithoutAuthorization() {
            long initialCount = externalAPIRepository.count();

            expect()
                .statusCode(403)
                .given()
                .body(EXTERNAL_API_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post();

            // Verify no changes when unauthorized
            assertEquals(initialCount, externalAPIRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(ExternalAPIController.class)
    @WithAdminUser
    class DeleteExternalAPI {
        @Test
        void shouldDeleteExternalAPIWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api);

            long initialCount = externalAPIRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", api.getUuid());

            // Verify deletion
            assertFalse(externalAPIRepository
                            .findByIdOptional(api.getUuid())
                            .isPresent());
            assertEquals(initialCount - 1, externalAPIRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenDeletingExternalAPIWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api);

            long initialCount = externalAPIRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .when()
                .delete("{uuid}", api.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, externalAPIRepository.count());
            assertTrue(externalAPIRepository
                           .findByIdOptional(api.getUuid())
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(ExternalAPIController.class)
    @WithAdminUser
    class SearchExternalAPIs {
        @Test
        void shouldReturnAllExternalAPIsWhenSearching() {
            QuarkusTransaction.begin();

            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api1 =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api1.setName("API 1");
            api1.setProductMappings(new java.util.HashMap<>());
            api1.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api1);

            de.iks.grocery_manager.server.model.mdi.ExternalAPI api2 =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api2.setName("API 2");
            api2.setProductMappings(new java.util.HashMap<>());
            api2.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api2);

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1))
                .body("content.size()", is(2))
                .body("content.find { it.uuid == '%s' }", withArgs(api1.getUuid()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(api2.getUuid()), notNullValue())
                .when()
                .get();
        }

        @Test
        void shouldReturnFilteredExternalAPIsWhenSearchingByName() {
            QuarkusTransaction.begin();

            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api1 =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api1.setName("Test API 1");
            api1.setProductMappings(new java.util.HashMap<>());
            api1.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api1);

            de.iks.grocery_manager.server.model.mdi.ExternalAPI api2 =
                new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api2.setName("Other API");
            api2.setProductMappings(new java.util.HashMap<>());
            api2.setStoreMappings(new java.util.HashMap<>());
            externalAPIRepository.persist(api2);

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(1))
                .body("page.totalPages", is(1))
                .body("content.size()", is(1))
                .body("content[0].uuid", isUuidOf(api1))
                .given()
                .queryParam("name", "Test")
                .get();
        }
    }
}
