package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.controller.masterdata.WithAdminUser;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.jpa.mdi.StoreEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.AddressPaths;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import de.iks.grocery_manager.server.model.mdi.ResponseType;
import de.iks.grocery_manager.server.model.mdi.StoreEndpoint;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.regex.Pattern;

import static de.iks.grocery_manager.server.UUIDMatcher.isUuidOf;
import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.withArgs;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(StoreEndpointController.class)
@WithAdminUser
@Sql("/testdata.sql")
class StoreEndpointControllerTest {
    private static final String STORE_ENDPOINT_1_CREATE_JSON = """
        {
          "name": "Store Endpoint 1",
          "baseUrl": "https://api.example.com",
          "pageSize": {
            "header": "Page-Size",
            "queryParameter": "pageSize"
          },
          "page": {
            "header": "Page",
            "queryParameter": "page"
          },
          "itemCount": {
            "header": "Item-Count",
            "queryParameter": "itemCount"
          },
          "basePath": "/stores",
          "storeIdPath": "$.id",
          "storeNamePath": "$.name",
          "storeLogoPath": "$.logo",
          "addressPath": "$.address",
          "addressPaths": {
            "countryPath": "$.country",
            "cityPath": "$.city",
            "zipPath": "$.zip",
            "streetPath": "$.street",
            "numberPath": "$.number"
          },
          "storeCurrencyPath": "$.currency",
          "responseType": "JSON"
        }""";
    private static final String STORE_ENDPOINT_1_UPDATE_JSON = """
        {
          "name": "Store Endpoint 1 Updated",
          "baseUrl": "https://api.example.com",
          "pageSize": {
            "header": "Page-Size",
            "queryParameter": "pageSize"
          },
          "page": {
            "header": "Page",
            "queryParameter": "page"
          },
          "itemCount": {
            "header": "Item-Count",
            "queryParameter": "itemCount"
          },
          "basePath": "/stores",
          "storeIdPath": "$.id",
          "storeNamePath": "$.name",
          "storeLogoPath": "$.logo",
          "addressPath": "$.address",
          "addressPaths": {
            "countryPath": "$.country",
            "cityPath": "$.city",
            "zipPath": "$.zip",
            "streetPath": "$.street",
            "numberPath": "$.number"
          },
          "storeCurrencyPath": "$.currency",
          "responseType": "JSON"
        }""";


    @Inject
    StoreEndpointRepository storeEndpointRepository;

    @Inject
    ExternalAPIRepository externalAPIRepository;

    @TestHTTPResource
    String baseURI;

    String baseURI(ExternalAPI parentApi) {
        return baseURI.replace(
            "{parentUuid}",
            parentApi
                .getUuid()
                .toString()
        );
    }

    @Nested
    @TestHTTPEndpoint(StoreEndpointController.class)
    @WithAdminUser
    class GetStoreEndpoint {
        @Test
        void shouldReturnStoreEndpointWhenFound() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            StoreEndpoint endpoint = new StoreEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Store Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/stores");
            endpoint.setStoreIdPath("$.id");
            AddressPaths addressPaths = new AddressPaths();
            addressPaths.setCountryPath("$.country");
            addressPaths.setCityPath("$.city");
            addressPaths.setZipPath("$.zip");
            addressPaths.setStreetPath("$.street");
            addressPaths.setNumberPath("$.number");
            endpoint.setAddressPaths(addressPaths);
            endpoint.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint);

            storeEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(endpoint))
                .body("name", is("Test Store Endpoint"))
                .when()
                .get("{uuid}", parentApi.getUuid(), endpoint.getUuid());
        }

        @Test
        void shouldReturn404WhenStoreEndpointNotFound() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .when()
                .get("{uuid}", parentApi.getUuid(), Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreEndpointController.class)
    @WithAdminUser
    class UpdateStoreEndpoint {
        @Test
        void shouldUpdateStoreEndpointWhenAuthorizedAndFound() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            StoreEndpoint endpoint = new StoreEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Store Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/stores");
            endpoint.setStoreIdPath("$.id");
            AddressPaths addressPaths = new AddressPaths();
            addressPaths.setCountryPath("$.country");
            addressPaths.setCityPath("$.city");
            addressPaths.setZipPath("$.zip");
            addressPaths.setStreetPath("$.street");
            addressPaths.setNumberPath("$.number");
            endpoint.setAddressPaths(addressPaths);
            endpoint.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint);

            long initialCount = storeEndpointRepository.count();

            storeEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(endpoint))
                .body("name", is("Store Endpoint 1 Updated"))
                .given()
                .body(STORE_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify update was applied
            assertTrue(storeEndpointRepository
                           .findByIdOptional(endpoint.getUuid())
                           .isPresent());
            assertEquals(
                "Store Endpoint 1 Updated",
                storeEndpointRepository
                    .findByIdOptional(endpoint.getUuid())
                    .get()
                    .getName()
            );
            assertEquals(initialCount, storeEndpointRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentStoreEndpoint() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = storeEndpointRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .given()
                .body(STORE_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), Testdata.BAD_UUID);

            // Verify no changes
            assertEquals(initialCount, storeEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenUpdatingStoreEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            StoreEndpoint endpoint = new StoreEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Store Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/stores");
            endpoint.setStoreIdPath("$.id");
            AddressPaths addressPaths = new AddressPaths();
            addressPaths.setCountryPath("$.country");
            addressPaths.setCityPath("$.city");
            addressPaths.setZipPath("$.zip");
            addressPaths.setStreetPath("$.street");
            addressPaths.setNumberPath("$.number");
            endpoint.setAddressPaths(addressPaths);
            endpoint.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint);

            long initialCount = storeEndpointRepository.count();

            storeEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .body(STORE_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeEndpointRepository.count());
            assertEquals(
                "Test Store Endpoint",
                storeEndpointRepository
                    .findByIdOptional(endpoint.getUuid())
                    .orElseThrow()
                    .getName()
            );
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreEndpointController.class)
    @WithAdminUser
    class CreateStoreEndpoint {
        @Test
        void shouldCreateStoreEndpointWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = storeEndpointRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(201)
                .header(
                    "location",
                    matchesRegex(String.format(
                        "%s/%s",
                        Pattern.quote(baseURI(parentApi)),
                        Testdata.UUID_PATTERN.pattern()
                    ))
                )
                .contentType(ContentType.JSON)
                .body("name", is("Store Endpoint 1"))
                .given()
                .body(STORE_ENDPOINT_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post("", parentApi.getUuid());

            // Verify creation
            assertEquals(initialCount + 1, storeEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenCreatingStoreEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = storeEndpointRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .body(STORE_ENDPOINT_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post("", parentApi.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeEndpointRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreEndpointController.class)
    @WithAdminUser
    class DeleteStoreEndpoint {
        @Test
        void shouldDeleteStoreEndpointWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            StoreEndpoint endpoint = new StoreEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Store Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/stores");
            endpoint.setStoreIdPath("$.id");
            AddressPaths addressPaths = new AddressPaths();
            addressPaths.setCountryPath("$.country");
            addressPaths.setCityPath("$.city");
            addressPaths.setZipPath("$.zip");
            addressPaths.setStreetPath("$.street");
            addressPaths.setNumberPath("$.number");
            endpoint.setAddressPaths(addressPaths);
            endpoint.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint);

            long initialCount = storeEndpointRepository.count();

            storeEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify deletion
            assertFalse(storeEndpointRepository
                            .findByIdOptional(endpoint.getUuid())
                            .isPresent());
            assertEquals(initialCount - 1, storeEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenDeletingStoreEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            StoreEndpoint endpoint = new StoreEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Store Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/stores");
            endpoint.setStoreIdPath("$.id");
            AddressPaths addressPaths = new AddressPaths();
            addressPaths.setCountryPath("$.country");
            addressPaths.setCityPath("$.city");
            addressPaths.setZipPath("$.zip");
            addressPaths.setStreetPath("$.street");
            addressPaths.setNumberPath("$.number");
            endpoint.setAddressPaths(addressPaths);
            endpoint.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint);

            long initialCount = storeEndpointRepository.count();

            storeEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .when()
                .delete("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeEndpointRepository.count());
            assertTrue(storeEndpointRepository
                           .findByIdOptional(endpoint.getUuid())
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreEndpointController.class)
    @WithAdminUser
    class SearchStoreEndpoints {
        @Test
        void shouldReturnAllStoreEndpointsWhenSearching() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            StoreEndpoint endpoint1 = new StoreEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Store Endpoint 1");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/stores");
            endpoint1.setStoreIdPath("$.id");
            AddressPaths addressPaths1 = new AddressPaths();
            addressPaths1.setCountryPath("$.country");
            addressPaths1.setCityPath("$.city");
            addressPaths1.setZipPath("$.zip");
            addressPaths1.setStreetPath("$.street");
            addressPaths1.setNumberPath("$.number");
            endpoint1.setAddressPaths(addressPaths1);
            endpoint1.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint1);

            StoreEndpoint endpoint2 = new StoreEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Store Endpoint 2");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/stores");
            endpoint2.setStoreIdPath("$.id");
            AddressPaths addressPaths2 = new AddressPaths();
            addressPaths2.setCountryPath("$.country");
            addressPaths2.setCityPath("$.city");
            addressPaths2.setZipPath("$.zip");
            addressPaths2.setStreetPath("$.street");
            addressPaths2.setNumberPath("$.number");
            endpoint2.setAddressPaths(addressPaths2);
            endpoint2.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint2);

            storeEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalPages", is(1))
                .body("page.totalElements", is(2))
                .body("content.size()", is(2))
                .body("content.find { it.uuid == '%s' }.name", withArgs(endpoint1.getUuid()), is("Store Endpoint 1"))
                .body("content.find { it.uuid == '%s' }.name", withArgs(endpoint2.getUuid()), is("Store Endpoint 2"))
                .given()
                .get("", parentApi.getUuid());
        }

        @Test
        void shouldReturnFilteredStoreEndpointsWhenSearchingByName() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            StoreEndpoint endpoint1 = new StoreEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Test Store Endpoint");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/stores");
            endpoint1.setStoreIdPath("$.id");
            AddressPaths addressPaths1 = new AddressPaths();
            addressPaths1.setCountryPath("$.country");
            addressPaths1.setCityPath("$.city");
            addressPaths1.setZipPath("$.zip");
            addressPaths1.setStreetPath("$.street");
            addressPaths1.setNumberPath("$.number");
            endpoint1.setAddressPaths(addressPaths1);
            endpoint1.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint1);

            StoreEndpoint endpoint2 = new StoreEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Other Endpoint");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/stores");
            endpoint2.setStoreIdPath("$.id");
            AddressPaths addressPaths2 = new AddressPaths();
            addressPaths2.setCountryPath("$.country");
            addressPaths2.setCityPath("$.city");
            addressPaths2.setZipPath("$.zip");
            addressPaths2.setStreetPath("$.street");
            addressPaths2.setNumberPath("$.number");
            endpoint2.setAddressPaths(addressPaths2);
            endpoint2.setResponseType(ResponseType.JSON);
            storeEndpointRepository.persist(endpoint2);

            storeEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalPages", is(1))
                .body("page.totalElements", is(1))
                .body("content.size()", is(1))
                .body("content.find { it.uuid == '%s' }.name", withArgs(endpoint1.getUuid()), is("Test Store Endpoint"))
                .given()
                .queryParam("name", "Test")
                .get("", parentApi.getUuid());
        }
    }
}
