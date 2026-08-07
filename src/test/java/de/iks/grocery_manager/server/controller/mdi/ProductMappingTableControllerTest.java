package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.controller.masterdata.WithAdminUser;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.locks.EntityAccess;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.iks.grocery_manager.server.UUIDMatcher.isUuid;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(ProductMappingTableController.class)
@WithAdminUser
@EntityAccess(ExternalAPIRepository.class)
class ProductMappingTableControllerTest {
    @Inject
    ExternalAPIRepository externalAPIRepository;

    @Inject
    ProductRepository productRepository;

    @Nested
    @TestHTTPEndpoint(ProductMappingTableController.class)
    @WithAdminUser
    class TranslateInbound {
        @Test
        void shouldReturnLocalIdWhenMappingExists() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mapping
            api
                .getProductMappings()
                .put(
                    productRepository
                        .findByIdOptional(Testdata.PRODUCT_1_UUID)
                        .orElseThrow(),
                    "remote_product_1"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is(String.format("\"%s\"", Testdata.PRODUCT_1_UUID)))
                .when()
                .get("in/{remoteId}", api.getUuid(), "remote_product_1");
        }

        @Test
        void shouldReturn204WhenMappingDoesNotExist() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(204)
                .when()
                .get("in/{remoteId}", api.getUuid(), "nonexistent_remote_id");
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mapping
            api
                .getProductMappings()
                .put(
                    productRepository
                        .findByIdOptional(Testdata.PRODUCT_1_UUID)
                        .orElseThrow(),
                    "remote_product_1"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .when()
                .get("in/{remoteId}", Testdata.BAD_UUID, "remote_product_1");
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductMappingTableController.class)
    @WithAdminUser
    class MassTranslateInbound {
        @Test
        void shouldReturnLocalIdsWhenMappingsExists() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mapping
            api
                .getProductMappings()
                .putAll(
                    Map.of(
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_1_UUID)
                            .orElseThrow(),
                        "remote_product_1",
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_2_UUID)
                            .orElseThrow(),
                        "remote_product_2",
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_3_UUID)
                            .orElseThrow(),
                        "remote_product_3"

                    )
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("%s", withArgs("remote_product_1"), isUuid(Testdata.PRODUCT_1_UUID))
                .body("%s", withArgs("remote_product_2"), isUuid(Testdata.PRODUCT_2_UUID))
                .given()
                .body(List.of("remote_product_1", "remote_product_2"))
                .contentType(ContentType.JSON)
                .request("QUERY", "in", api.getUuid());
        }

        @Test
        void shouldReturnEmptyWhenMappingsDoNotExist() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("{}"))
                .given()
                .body(List.of("nonexistent_remote_id_1", "nonexistent_remote_id_2"))
                .contentType(ContentType.JSON)
                .request("QUERY", "in", api.getUuid());
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mapping
            api
                .getProductMappings()
                .put(
                    productRepository
                        .findByIdOptional(Testdata.PRODUCT_1_UUID)
                        .orElseThrow(),
                    "remote_product_1"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .given()
                .body(List.of("remote_product_1", "nonexistent_remote_id_2"))
                .contentType(ContentType.JSON)
                .request("QUERY", "in", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductMappingTableController.class)
    @WithAdminUser
    class SetInboundTranslation {
        @Test
        void shouldSetMappingWhenValid() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            String localIdJson = "\"" + Testdata.PRODUCT_1_UUID + "\"";

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is(String.format("\"%s\"", Testdata.PRODUCT_1_UUID)))
                .given()
                .body(localIdJson)
                .contentType(ContentType.JSON)
                .put("in/{remoteId}", api.getUuid(), "remote_product_1");

            api = externalAPIRepository
                .findByIdOptional(api.getUuid())
                .orElseThrow();

            // Verify mapping was set
            assertEquals(
                "remote_product_1",
                api
                    .getProductMappings()
                    .get(
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_1_UUID)
                            .orElseThrow()
                    )
            );
        }

        @Test
        void shouldReturn404WhenLocalProductNotFound() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            String localIdJson = "\"" + Testdata.BAD_UUID + "\"";

            expect()
                .statusCode(404)
                .given()
                .body(localIdJson)
                .contentType(ContentType.JSON)
                .put("in/{remoteId}", api.getUuid(), "remote_product_1");
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            String localIdJson = "\"" + Testdata.PRODUCT_1_UUID + "\"";

            expect()
                .statusCode(404)
                .given()
                .body(localIdJson)
                .contentType(ContentType.JSON)
                .put("in/{remoteId}", Testdata.BAD_UUID, "nonexistent_remote_id");
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductMappingTableController.class)
    @WithAdminUser
    class TranslateOutbound {
        @Test
        void shouldReturnRemoteIdWhenMappingExists() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mapping
            api
                .getProductMappings()
                .put(
                    productRepository
                        .findByIdOptional(Testdata.PRODUCT_1_UUID)
                        .orElseThrow(),
                    "remote_product_1"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("\"remote_product_1\""))
                .when()
                .get("out/{localId}", api.getUuid(), Testdata.PRODUCT_1_UUID);
        }

        @Test
        void shouldReturn204WhenMappingDoesNotExist() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(204)
                .when()
                .get("out/{localId}", api.getUuid(), Testdata.PRODUCT_1_UUID);
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("out/{localId}", Testdata.BAD_UUID, Testdata.PRODUCT_1_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductMappingTableController.class)
    @WithAdminUser
    class MassTranslateOutbound {
        @Test
        void shouldReturnLocalIdsWhenMappingsExists() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mapping
            api
                .getProductMappings()
                .putAll(
                    Map.of(
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_1_UUID)
                            .orElseThrow(),
                        "remote_product_1",
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_2_UUID)
                            .orElseThrow(),
                        "remote_product_2",
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_3_UUID)
                            .orElseThrow(),
                        "remote_product_3"

                    )
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("%s", withArgs(Testdata.PRODUCT_1_UUID), is("remote_product_1"))
                .body("%s", withArgs(Testdata.PRODUCT_2_UUID), is("remote_product_2"))
                .given()
                .body(List.of(Testdata.PRODUCT_1_UUID, Testdata.PRODUCT_2_UUID))
                .contentType(ContentType.JSON)
                .request("QUERY", "out", api.getUuid());
        }

        @Test
        void shouldReturnEmptyWhenMappingsDoNotExist() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("{}"))
                .given()
                .body(List.of(Testdata.PRODUCT_1_UUID, Testdata.PRODUCT_2_UUID))
                .contentType(ContentType.JSON)
                .request("QUERY", "in", api.getUuid());
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mapping
            api
                .getProductMappings()
                .put(
                    productRepository
                        .findByIdOptional(Testdata.PRODUCT_1_UUID)
                        .orElseThrow(),
                    "remote_product_1"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(404)
                .given()
                .body(List.of(Testdata.PRODUCT_1_UUID, Testdata.PRODUCT_2_UUID))
                .contentType(ContentType.JSON)
                .request("QUERY", "in", Testdata.BAD_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductMappingTableController.class)
    @WithAdminUser
    class SetOutboundTranslation {
        @Test
        void shouldSetMappingWhenValid() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            String remoteIdJson = "\"remote_product_1\"";

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is(remoteIdJson))
                .given()
                .body(remoteIdJson)
                .contentType(ContentType.JSON)
                .put("out/{localId}", api.getUuid(), Testdata.PRODUCT_1_UUID);


            api = externalAPIRepository
                .findByIdOptional(api.getUuid())
                .orElseThrow();

            // Verify mapping was set
            assertEquals(
                "remote_product_1",
                api
                    .getProductMappings()
                    .get(
                        productRepository
                            .findByIdOptional(Testdata.PRODUCT_1_UUID)
                            .orElseThrow()
                    )
            );
        }

        @Test
        void shouldReturn404WhenLocalProductNotFound() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            String remoteIdJson = "\"remote_product_1\"";

            expect()
                .statusCode(404)
                .given()
                .body(remoteIdJson)
                .contentType(ContentType.JSON)
                .put("out/{localId}", api.getUuid(), Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            String remoteIdJson = "\"remote_product_1\"";

            expect()
                .statusCode(404)
                .given()
                .body(remoteIdJson)
                .contentType(ContentType.JSON)
                .put("out/{localId}", Testdata.BAD_UUID, Testdata.PRODUCT_1_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(ProductMappingTableController.class)
    @WithAdminUser
    class GetMappings {
        @Test
        void shouldReturnMappingsWhenMappingsExist() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            // Set up mappings
            api
                .getProductMappings()
                .put(
                    productRepository
                        .findByIdOptional(Testdata.PRODUCT_1_UUID)
                        .orElseThrow(),
                    "remote_product_1"
                );
            api
                .getProductMappings()
                .put(
                    productRepository
                        .findByIdOptional(Testdata.PRODUCT_2_UUID)
                        .orElseThrow(),
                    "remote_product_2"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("%s", withArgs(Testdata.PRODUCT_1_UUID), is("remote_product_1"))
                .body("%s", withArgs(Testdata.PRODUCT_2_UUID), is("remote_product_2"))
                .when()
                .get("", api.getUuid());
        }

        @Test
        void shouldReturnEmptyMapWhenNoMappingsExist() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("{}"))
                .when()
                .get("", api.getUuid());
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("", Testdata.BAD_UUID);
        }
    }
}
