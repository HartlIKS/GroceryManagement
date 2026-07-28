package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.controller.masterdata.WithAdminUser;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.jpa.mdi.ProductEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import de.iks.grocery_manager.server.model.mdi.ProductEndpoint;
import de.iks.grocery_manager.server.model.mdi.ResponseType;
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

import static de.iks.grocery_manager.server.UUIDMatcher.*;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(ProductEndpointController.class)
@WithAdminUser
@Sql("/testdata.sql")
class ProductEndpointControllerTest {

    @Inject
    ProductEndpointRepository productEndpointRepository;

    @Inject
    ExternalAPIRepository externalAPIRepository;

    @TestHTTPResource
    String baseURI;

    String baseURI(ExternalAPI externalAPI) {
        return baseURI.replace(
            "{parentUuid}",
            externalAPI
                .getUuid()
                .toString()
        );
    }

    @Nested
    @TestHTTPEndpoint(ProductEndpointController.class)
    @WithAdminUser
    class GetProductEndpoint {
        @Test
        void shouldReturnProductEndpointWhenFound() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint);

            productEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(endpoint))
                .body("name", is("Test Product Endpoint"))
                .when()
                .get("{uuid}", parentApi.getUuid(), endpoint.getUuid());
        }

        @Test
        void shouldReturn404WhenProductEndpointNotFound() {
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
    @TestHTTPEndpoint(ProductEndpointController.class)
    @WithAdminUser
    class UpdateProductEndpoint {
        private static final String PRODUCT_ENDPOINT_1_UPDATE_JSON = """
        {
          "name": "Product Endpoint 1 Updated",
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
          "basePath": "/products",
          "productIdPath": "$.id",
          "productNamePath": "$.name",
          "productImagePath": "$.image",
          "productEANPath": "$.ean",
          "responseType": "JSON"
        }""";

        @Test
        void shouldUpdateProductEndpointWhenAuthorizedAndFound() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint);

            long initialCount = productEndpointRepository.count();

            productEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(endpoint))
                .body("name", is("Product Endpoint 1 Updated"))
                .given()
                .body(PRODUCT_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify update was applied
            assertTrue(productEndpointRepository
                           .findByIdOptional(endpoint.getUuid())
                           .isPresent());
            assertEquals(
                "Product Endpoint 1 Updated",
                productEndpointRepository
                    .findByIdOptional(endpoint.getUuid())
                    .get()
                    .getName()
            );
            assertEquals(initialCount, productEndpointRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentProductEndpoint() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = productEndpointRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .given()
                .body(PRODUCT_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), Testdata.BAD_UUID);

            // Verify no changes
            assertEquals(initialCount, productEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenUpdatingProductEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint);

            long initialCount = productEndpointRepository.count();

            productEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .body(PRODUCT_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, productEndpointRepository.count());
            assertEquals(
                "Test Product Endpoint",
                productEndpointRepository
                    .findByIdOptional(endpoint.getUuid())
                    .orElseThrow()
                    .getName()
            );
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductEndpointController.class)
    @WithAdminUser
    class CreateProductEndpoint {
        private static final String PRODUCT_ENDPOINT_1_CREATE_JSON = """
        {
          "name": "Product Endpoint 1",
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
          "basePath": "/products",
          "productIdPath": "$.id",
          "productNamePath": "$.name",
          "productImagePath": "$.image",
          "productEANPath": "$.ean",
          "responseType": "JSON"
        }""";

        @Test
        void shouldCreateProductEndpointWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = productEndpointRepository.count();

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
                .body("name", is("Product Endpoint 1"))
                .given()
                .body(PRODUCT_ENDPOINT_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post("", parentApi.getUuid());

            // Verify creation
            assertEquals(initialCount + 1, productEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenCreatingProductEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = productEndpointRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .body(PRODUCT_ENDPOINT_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post("", parentApi.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, productEndpointRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductEndpointController.class)
    @WithAdminUser
    class DeleteProductEndpoint {
        @Test
        void shouldDeleteProductEndpointWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint);

            long initialCount = productEndpointRepository.count();

            productEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify deletion
            assertFalse(productEndpointRepository
                            .findByIdOptional(endpoint.getUuid())
                            .isPresent());
            assertEquals(initialCount - 1, productEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenDeletingProductEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint);

            long initialCount = productEndpointRepository.count();

            productEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .when()
                .delete("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, productEndpointRepository.count());
            assertTrue(productEndpointRepository
                           .findByIdOptional(endpoint.getUuid())
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductEndpointController.class)
    @WithAdminUser
    class SearchProductEndpoints {
        @Test
        void shouldReturnAllProductEndpointsWhenSearching() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            ProductEndpoint endpoint1 = new ProductEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Product Endpoint 1");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/products");
            endpoint1.setProductIdPath("$.id");
            endpoint1.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint1);

            ProductEndpoint endpoint2 = new ProductEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Product Endpoint 2");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/products");
            endpoint2.setProductIdPath("$.id");
            endpoint2.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint2);

            productEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(2))
                .body("page.totalPages", is(1))
                .body("content.size()", is(2))
                .body("content.find { it.uuid == '%s' }.name", withArgs(endpoint1.getUuid()), is("Product Endpoint 1"))
                .body("content.find { it.uuid == '%s' }.name", withArgs(endpoint2.getUuid()), is("Product Endpoint 2"))
                .given()
                .get("", parentApi.getUuid());
        }

        @Test
        void shouldReturnFilteredProductEndpointsWhenSearchingByName() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            ProductEndpoint endpoint1 = new ProductEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Test Product Endpoint");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/products");
            endpoint1.setProductIdPath("$.id");
            endpoint1.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint1);

            ProductEndpoint endpoint2 = new ProductEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Other Endpoint");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/products");
            endpoint2.setProductIdPath("$.id");
            endpoint2.setResponseType(ResponseType.JSON);
            productEndpointRepository.persist(endpoint2);

            productEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalElements", is(1))
                .body("page.totalPages", is(1))
                .body("content.size()", is(1))
                .body("content.find { it.uuid = '%s' }.name", withArgs(endpoint1.getUuid()), is("Test Product Endpoint"))
                .given()
                .queryParam("name", "Test")
                .get("", parentApi.getUuid());
        }
    }
}
