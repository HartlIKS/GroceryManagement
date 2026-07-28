package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Sql;
import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.controller.masterdata.WithAdminUser;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@TestHTTPEndpoint(StoreMappingTableController.class)
@WithAdminUser
@Sql("/testdata.sql")
class StoreMappingTableControllerTest {
    @Inject
    ExternalAPIRepository externalAPIRepository;

    @Inject
    StoreRepository storeRepository;

    @Nested
    @TestHTTPEndpoint(StoreMappingTableController.class)
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

            // Set up mapping
            api
                .getStoreMappings()
                .put(
                    storeRepository
                        .findByIdOptional(Testdata.STORE_1_UUID)
                        .orElseThrow(),
                    "remote_store_1"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is(String.format("\"%s\"", Testdata.STORE_1_UUID)))
                .when()
                .get("in/{remoteId}", api.getUuid(), "remote_store_1");
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
            expect()
                .statusCode(404)
                .when()
                .get("in/{remoteId}", Testdata.BAD_UUID, "remote_id");
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreMappingTableController.class)
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

            String localIdJson = "\"" + Testdata.STORE_1_UUID + "\"";

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is(localIdJson))
                .given()
                .body(localIdJson)
                .contentType(ContentType.JSON)
                .put("in/{remoteId}", api.getUuid(), "remote_store_1");

            // Verify mapping was set
            api = externalAPIRepository
                .findByIdOptional(api.getUuid())
                .orElseThrow();
            assertEquals(
                "remote_store_1",
                api
                    .getStoreMappings()
                    .get(
                        storeRepository
                            .findByIdOptional(Testdata.STORE_1_UUID)
                            .orElseThrow()
                    )
            );
        }

        @Test
        void shouldReturn404WhenLocalStoreNotFound() {
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
                .put("in/{remoteId}", api.getUuid(), "remote_store_1");
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            String localIdJson = "\"" + Testdata.STORE_1_UUID + "\"";

            expect()
                .statusCode(404)
                .given()
                .body(localIdJson)
                .contentType(ContentType.JSON)
                .put("in/{remoteId}", Testdata.BAD_UUID, "remote_store_1");
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreMappingTableController.class)
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

            // Set up mapping
            api
                .getStoreMappings()
                .put(
                    storeRepository
                        .findByIdOptional(Testdata.STORE_1_UUID)
                        .orElseThrow(),
                    "remote_store_1"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is("\"remote_store_1\""))
                .when()
                .get("out/{localId}", api.getUuid(), Testdata.STORE_1_UUID);
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
                .get("out/{localId}", api.getUuid(), Testdata.STORE_1_UUID);
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            expect()
                .statusCode(404)
                .when()
                .get("out/{localId}", Testdata.BAD_UUID, Testdata.STORE_1_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreMappingTableController.class)
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

            String remoteIdJson = "\"remote_store_1\"";

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(is(remoteIdJson))
                .given()
                .body(remoteIdJson)
                .contentType(ContentType.JSON)
                .put("out/{localId}", api.getUuid(), Testdata.STORE_1_UUID);

            // Verify mapping was set
            api = externalAPIRepository
                .findByIdOptional(api.getUuid())
                .orElseThrow();
            assertEquals(
                "remote_store_1",
                api
                    .getStoreMappings()
                    .get(
                        storeRepository
                            .findByIdOptional(Testdata.STORE_1_UUID)
                            .orElseThrow()
                    )
            );
        }

        @Test
        void shouldReturn404WhenLocalStoreNotFound() {
            QuarkusTransaction.begin();

            // Create ExternalAPI
            ExternalAPI api = new ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new HashMap<>());
            api.setStoreMappings(new HashMap<>());
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            String remoteIdJson = "\"remote_store_1\"";

            expect()
                .statusCode(404)
                .given()
                .body(remoteIdJson)
                .contentType(ContentType.JSON)
                .put("out/{localId}", api.getUuid(), Testdata.BAD_UUID);
        }

        @Test
        void shouldReturn404WhenApiNotFound() {
            String remoteIdJson = "\"remote_store_1\"";

            expect()
                .statusCode(404)
                .given()
                .body(remoteIdJson)
                .contentType(ContentType.JSON)
                .put("out/{localId}", Testdata.BAD_UUID, Testdata.STORE_1_UUID);
        }
    }

    @Nested
    @TestHTTPEndpoint(StoreMappingTableController.class)
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

            // Set up mappings
            api
                .getStoreMappings()
                .put(
                    storeRepository
                        .findByIdOptional(Testdata.STORE_1_UUID)
                        .orElseThrow(),
                    "remote_store_1"
                );
            api
                .getStoreMappings()
                .put(
                    storeRepository
                        .findByIdOptional(Testdata.STORE_2_UUID)
                        .orElseThrow(),
                    "remote_store_2"
                );
            externalAPIRepository.persistAndFlush(api);

            QuarkusTransaction.commit();

            expect()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("%s", withArgs(Testdata.STORE_1_UUID), is("remote_store_1"))
                .body("%s", withArgs(Testdata.STORE_2_UUID), is("remote_store_2"))
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
