package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.WithTestUser;
import de.iks.grocery_manager.server.controller.masterdata.WithAdminUser;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.jpa.mdi.PriceEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import de.iks.grocery_manager.server.model.mdi.PriceEndpoint;
import de.iks.grocery_manager.server.model.mdi.ResponseType;
import de.iks.grocery_manager.server.model.mdi.handling.Parameter;
import de.iks.grocery_manager.server.model.mdi.handling.ProductHandlingType;
import de.iks.grocery_manager.server.model.mdi.handling.StoreHandlingType;
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
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(PriceEndpointController.class)
@WithAdminUser
@Sql("/testdata.sql")
class PriceEndpointControllerTest {

    @Inject
    PriceEndpointRepository priceEndpointRepository;

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
    @TestHTTPEndpoint(PriceEndpointController.class)
    @WithAdminUser
    class GetPriceEndpoint {
        @Test
        void shouldReturnPriceEndpointWhenFound() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            PriceEndpoint endpoint = new PriceEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Price Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/prices");
            endpoint.setPricePath("$.price");
            endpoint.setTimeFormat("yyyy-MM-dd");
            endpoint.setValidFromPath("$.validFrom");
            endpoint.setValidUntilPath("$.validUntil");
            endpoint.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam = new Parameter();
            productParam.setHeader("Product-Header");
            productParam.setQueryParameter("productId");
            endpoint.setProductParameters(productParam);
            endpoint.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam = new Parameter();
            storeParam.setHeader("Store-Header");
            storeParam.setQueryParameter("storeId");
            endpoint.setStoreParameters(storeParam);
            endpoint.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint);

            priceEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(endpoint))
                .body("name", is("Test Price Endpoint"))
                .when()
                .get("{uuid}", parentApi.getUuid(), endpoint.getUuid());
        }

        @Test
        void shouldReturn404WhenPriceEndpointNotFound() {
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
    @TestHTTPEndpoint(PriceEndpointController.class)
    @WithAdminUser
    class UpdatePriceEndpoint {
        private static final String PRICE_ENDPOINT_1_UPDATE_JSON = """
            {
              "name": "Price Endpoint 1 Updated",
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
              "basePath": "/prices",
              "productHandling": {
                "type": "path",
                "path": "/product"
              },
              "storeHandling": {
                "type": "oneForAll"
              },
              "pricePath": "$.price",
              "timeFormat": "yyyy-MM-dd",
              "validFromPath": "$.validFrom",
              "validUntilPath": "$.validUntil",
              "responseType": "JSON"
            }""";

        @Test
        void shouldUpdatePriceEndpointWhenAuthorizedAndFound() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            PriceEndpoint endpoint = new PriceEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Price Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/prices");
            endpoint.setPricePath("$.price");
            endpoint.setTimeFormat("yyyy-MM-dd");
            endpoint.setValidFromPath("$.validFrom");
            endpoint.setValidUntilPath("$.validUntil");
            endpoint.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam = new Parameter();
            productParam.setHeader("Product-Header");
            productParam.setQueryParameter("productId");
            endpoint.setProductParameters(productParam);
            endpoint.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam = new Parameter();
            storeParam.setHeader("Store-Header");
            storeParam.setQueryParameter("storeId");
            endpoint.setStoreParameters(storeParam);
            endpoint.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint);

            long initialCount = priceEndpointRepository.count();

            priceEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("uuid", isUuidOf(endpoint))
                .body("name", is("Price Endpoint 1 Updated"))
                .given()
                .body(PRICE_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify update was applied
            assertTrue(priceEndpointRepository
                           .findByIdOptional(endpoint.getUuid())
                           .isPresent());
            assertEquals(
                "Price Endpoint 1 Updated",
                priceEndpointRepository
                    .findByIdOptional(endpoint.getUuid())
                    .get()
                    .getName()
            );
            assertEquals(initialCount, priceEndpointRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentPriceEndpoint() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = priceEndpointRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .given()
                .body(PRICE_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), Testdata.BAD_UUID);

            // Verify no changes
            assertEquals(initialCount, priceEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenUpdatingPriceEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            PriceEndpoint endpoint = new PriceEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Price Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/prices");
            endpoint.setPricePath("$.price");
            endpoint.setTimeFormat("yyyy-MM-dd");
            endpoint.setValidFromPath("$.validFrom");
            endpoint.setValidUntilPath("$.validUntil");
            endpoint.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam = new Parameter();
            productParam.setHeader("Product-Header");
            productParam.setQueryParameter("productId");
            endpoint.setProductParameters(productParam);
            endpoint.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam = new Parameter();
            storeParam.setHeader("Store-Header");
            storeParam.setQueryParameter("storeId");
            endpoint.setStoreParameters(storeParam);
            endpoint.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint);

            long initialCount = priceEndpointRepository.count();

            priceEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .body(PRICE_ENDPOINT_1_UPDATE_JSON)
                .contentType(ContentType.JSON)
                .put("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceEndpointRepository.count());
            assertEquals(
                "Test Price Endpoint",
                priceEndpointRepository
                    .findByIdOptional(endpoint.getUuid())
                    .orElseThrow()
                    .getName()
            );
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceEndpointController.class)
    @WithAdminUser
    class CreatePriceEndpoint {
        private static final String PRICE_ENDPOINT_1_CREATE_JSON = """
            {
              "name": "Price Endpoint 1",
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
              "basePath": "/prices",
              "productHandling": {
                "type": "parameter",
                "parameter": {
                  "header": "Product-Header",
                  "queryParameter": "productId"
                }
              },
              "storeHandling": {
                "type": "parameter",
                "parameter": {
                  "header": "Store-Header",
                  "queryParameter": "storeId"
                }
              },
              "pricePath": "$.price",
              "timeFormat": "yyyy-MM-dd",
              "validFromPath": "$.validFrom",
              "validUntilPath": "$.validUntil",
              "responseType": "JSON"
            }""";

        @Test
        void shouldCreatePriceEndpointWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = priceEndpointRepository.count();

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
                .body("name", is("Price Endpoint 1"))
                .given()
                .body(PRICE_ENDPOINT_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post("", parentApi.getUuid());

            // Verify creation
            assertEquals(initialCount + 1, priceEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenCreatingPriceEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            long initialCount = priceEndpointRepository.count();

            externalAPIRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .given()
                .body(PRICE_ENDPOINT_1_CREATE_JSON)
                .contentType(ContentType.JSON)
                .post("", parentApi.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceEndpointRepository.count());
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceEndpointController.class)
    @WithAdminUser
    class DeletePriceEndpoint {
        @Test
        void shouldDeletePriceEndpointWhenAuthorized() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            PriceEndpoint endpoint = new PriceEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Price Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/prices");
            endpoint.setPricePath("$.price");
            endpoint.setTimeFormat("yyyy-MM-dd");
            endpoint.setValidFromPath("$.validFrom");
            endpoint.setValidUntilPath("$.validUntil");
            endpoint.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam = new Parameter();
            productParam.setHeader("Product-Header");
            productParam.setQueryParameter("productId");
            endpoint.setProductParameters(productParam);
            endpoint.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam = new Parameter();
            storeParam.setHeader("Store-Header");
            storeParam.setQueryParameter("storeId");
            endpoint.setStoreParameters(storeParam);
            endpoint.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint);

            long initialCount = priceEndpointRepository.count();

            priceEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .when()
                .delete("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify deletion
            assertFalse(priceEndpointRepository
                            .findByIdOptional(endpoint.getUuid())
                            .isPresent());
            assertEquals(initialCount - 1, priceEndpointRepository.count());
        }

        @Test
        @WithTestUser
        void shouldReturn403WhenDeletingPriceEndpointWithoutAuthorization() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            PriceEndpoint endpoint = new PriceEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Price Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/prices");
            endpoint.setPricePath("$.price");
            endpoint.setTimeFormat("yyyy-MM-dd");
            endpoint.setValidFromPath("$.validFrom");
            endpoint.setValidUntilPath("$.validUntil");
            endpoint.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam = new Parameter();
            productParam.setHeader("Product-Header");
            productParam.setQueryParameter("productId");
            endpoint.setProductParameters(productParam);
            endpoint.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam = new Parameter();
            storeParam.setHeader("Store-Header");
            storeParam.setQueryParameter("storeId");
            endpoint.setStoreParameters(storeParam);
            endpoint.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint);

            long initialCount = priceEndpointRepository.count();

            priceEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(403)
                .when()
                .delete("{uuid}", parentApi.getUuid(), endpoint.getUuid());

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceEndpointRepository.count());
            assertTrue(priceEndpointRepository
                           .findByIdOptional(endpoint.getUuid())
                           .isPresent());
        }
    }

    @Nested
    @TestHTTPEndpoint(PriceEndpointController.class)
    @WithAdminUser
    class SearchPriceEndpoints {
        @Test
        void shouldReturnAllPriceEndpointsWhenSearching() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            PriceEndpoint endpoint1 = new PriceEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Price Endpoint 1");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/prices");
            endpoint1.setPricePath("$.price");
            endpoint1.setTimeFormat("yyyy-MM-dd");
            endpoint1.setValidFromPath("$.validFrom");
            endpoint1.setValidUntilPath("$.validUntil");
            endpoint1.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam1 = new Parameter();
            productParam1.setHeader("Product-Header");
            productParam1.setQueryParameter("productId");
            endpoint1.setProductParameters(productParam1);
            endpoint1.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam1 = new Parameter();
            storeParam1.setHeader("Store-Header");
            storeParam1.setQueryParameter("storeId");
            endpoint1.setStoreParameters(storeParam1);
            endpoint1.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint1);

            PriceEndpoint endpoint2 = new PriceEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Price Endpoint 2");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/prices");
            endpoint2.setPricePath("$.price");
            endpoint2.setTimeFormat("yyyy-MM-dd");
            endpoint2.setValidFromPath("$.validFrom");
            endpoint2.setValidUntilPath("$.validUntil");
            endpoint2.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam2 = new Parameter();
            productParam2.setHeader("Product-Header");
            productParam2.setQueryParameter("productId");
            endpoint2.setProductParameters(productParam2);
            endpoint2.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam2 = new Parameter();
            storeParam2.setHeader("Store-Header");
            storeParam2.setQueryParameter("storeId");
            endpoint2.setStoreParameters(storeParam2);
            endpoint2.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint2);

            priceEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalPages", is(1))
                .body("page.totalElements", is(2))
                .body("content.size()", is(2))
                .body("content.find { it.uuid == '%s' }", withArgs(endpoint1.getUuid()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(endpoint2.getUuid()), notNullValue())
                .when()
                .get("", parentApi.getUuid());
        }

        @Test
        void shouldReturnFilteredPriceEndpointsWhenSearchingByName() {
            QuarkusTransaction.begin();

            // Create test data
            ExternalAPI parentApi = new ExternalAPI();
            parentApi.setName("Test API");
            parentApi.setProductMappings(new HashMap<>());
            parentApi.setStoreMappings(new HashMap<>());
            externalAPIRepository.persist(parentApi);

            PriceEndpoint endpoint1 = new PriceEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Test Price Endpoint");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/prices");
            endpoint1.setPricePath("$.price");
            endpoint1.setTimeFormat("yyyy-MM-dd");
            endpoint1.setValidFromPath("$.validFrom");
            endpoint1.setValidUntilPath("$.validUntil");
            endpoint1.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam1 = new Parameter();
            productParam1.setHeader("Product-Header");
            productParam1.setQueryParameter("productId");
            endpoint1.setProductParameters(productParam1);
            endpoint1.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam1 = new Parameter();
            storeParam1.setHeader("Store-Header");
            storeParam1.setQueryParameter("storeId");
            endpoint1.setStoreParameters(storeParam1);
            endpoint1.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint1);

            PriceEndpoint endpoint2 = new PriceEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Other Endpoint");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/prices");
            endpoint2.setPricePath("$.price");
            endpoint2.setTimeFormat("yyyy-MM-dd");
            endpoint2.setValidFromPath("$.validFrom");
            endpoint2.setValidUntilPath("$.validUntil");
            endpoint2.setProductHandlingType(ProductHandlingType.PARAMETER);
            Parameter productParam2 = new Parameter();
            productParam2.setHeader("Product-Header");
            productParam2.setQueryParameter("productId");
            endpoint2.setProductParameters(productParam2);
            endpoint2.setStoreHandlingType(StoreHandlingType.PARAMETER);
            Parameter storeParam2 = new Parameter();
            storeParam2.setHeader("Store-Header");
            storeParam2.setQueryParameter("storeId");
            endpoint2.setStoreParameters(storeParam2);
            endpoint2.setResponseType(ResponseType.JSON);
            priceEndpointRepository.persist(endpoint2);

            priceEndpointRepository.flush();

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page.number", is(0))
                .body("page.size", is(10))
                .body("page.totalPages", is(1))
                .body("page.totalElements", is(1))
                .body("content.size()", is(1))
                .body("content.find { it.uuid == '%s' }", withArgs(endpoint1.getUuid()), notNullValue())
                .body("content.find { it.uuid == '%s' }", withArgs(endpoint2.getUuid()), nullValue())
                .given()
                .queryParam("name", "Test")
                .get("", parentApi.getUuid());
        }
    }
}
