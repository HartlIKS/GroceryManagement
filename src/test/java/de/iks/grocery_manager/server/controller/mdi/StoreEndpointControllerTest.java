package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.jpa.mdi.StoreEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.AddressPaths;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import de.iks.grocery_manager.server.model.mdi.ResponseType;
import de.iks.grocery_manager.server.model.mdi.StoreEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
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

    private MockMvc mockMvc;

    @Autowired
    private StoreEndpointRepository storeEndpointRepository;

    @Autowired
    private ExternalAPIRepository externalAPIRepository;

    @Autowired
    private AuthorityConfiguration authorityConfiguration;

    private RequestPostProcessor admin_jwt;
    private final RequestPostProcessor user_jwt = jwt();

    private ExternalAPI parentApi;

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        admin_jwt = jwt()
            .authorities(new SimpleGrantedAuthority(authorityConfiguration.getMasterdataAuthority()));
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();
        
        externalAPIRepository.deleteAll();
        storeEndpointRepository.deleteAll();

        // Create parent ExternalAPI
        parentApi = new ExternalAPI();
        parentApi.setName("Test API");
        parentApi.setProductMappings(new HashMap<>());
        parentApi.setStoreMappings(new HashMap<>());
        parentApi = externalAPIRepository.save(parentApi);
    }

    @Nested
    class GetStoreEndpoint {
        @Test
        void shouldReturnStoreEndpointWhenFound() throws Exception {
            // Create test data
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
            endpoint = storeEndpointRepository.save(endpoint);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/store/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(endpoint.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Test Store Endpoint"));
        }

        @Test
        void shouldReturn404WhenStoreEndpointNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/store/{uuid}", parentApi.getUuid(), Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateStoreEndpoint {
        @Test
        void shouldUpdateStoreEndpointWhenAuthorizedAndFound() throws Exception {
            // Create test data
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
            endpoint = storeEndpointRepository.save(endpoint);

            long initialCount = storeEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/store/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .content(STORE_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(endpoint.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Store Endpoint 1 Updated"));

            // Verify update was applied
            assertTrue(storeEndpointRepository.findById(endpoint.getUuid()).isPresent());
            assertEquals("Store Endpoint 1 Updated", storeEndpointRepository.findById(endpoint.getUuid()).get().getName());
            assertEquals(initialCount, storeEndpointRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentStoreEndpoint() throws Exception {
            long initialCount = storeEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/store/{uuid}", parentApi.getUuid(), Testdata.BAD_UUID)
                        .content(STORE_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());

            // Verify no changes
            assertEquals(initialCount, storeEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenUpdatingStoreEndpointWithoutAuthorization() throws Exception {
            // Create test data
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
            endpoint = storeEndpointRepository.save(endpoint);

            long initialCount = storeEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/store/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .content(STORE_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeEndpointRepository.count());
            assertEquals("Test Store Endpoint", storeEndpointRepository.findById(endpoint.getUuid()).get().getName());
        }
    }

    @Nested
    class CreateStoreEndpoint {
        @Test
        void shouldCreateStoreEndpointWhenAuthorized() throws Exception {
            long initialCount = storeEndpointRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface/{parentUuid}/endpoint/store", parentApi.getUuid())
                        .content(STORE_ENDPOINT_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/masterdata/interface/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}/endpoint/store/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Store Endpoint 1"));

            // Verify creation
            assertEquals(initialCount + 1, storeEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenCreatingStoreEndpointWithoutAuthorization() throws Exception {
            long initialCount = storeEndpointRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface/{parentUuid}/endpoint/store", parentApi.getUuid())
                        .content(STORE_ENDPOINT_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeEndpointRepository.count());
        }
    }

    @Nested
    class DeleteStoreEndpoint {
        @Test
        void shouldDeleteStoreEndpointWhenAuthorized() throws Exception {
            // Create test data
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
            endpoint = storeEndpointRepository.save(endpoint);

            long initialCount = storeEndpointRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{parentUuid}/endpoint/store/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());

            // Verify deletion
            assertFalse(storeEndpointRepository.findById(endpoint.getUuid()).isPresent());
            assertEquals(initialCount - 1, storeEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenDeletingStoreEndpointWithoutAuthorization() throws Exception {
            // Create test data
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
            endpoint = storeEndpointRepository.save(endpoint);

            long initialCount = storeEndpointRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{parentUuid}/endpoint/store/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, storeEndpointRepository.count());
            assertTrue(storeEndpointRepository.findById(endpoint.getUuid()).isPresent());
        }
    }

    @Nested
    class SearchStoreEndpoints {
        @Test
        void shouldReturnAllStoreEndpointsWhenSearching() throws Exception {
            // Create test data
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
            endpoint1 = storeEndpointRepository.save(endpoint1);

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
            endpoint2 = storeEndpointRepository.save(endpoint2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/store", parentApi.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Store Endpoint 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Store Endpoint 2')]").exists());
        }

        @Test
        void shouldReturnFilteredStoreEndpointsWhenSearchingByName() throws Exception {
            // Create test data
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
            endpoint1 = storeEndpointRepository.save(endpoint1);

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
            endpoint2 = storeEndpointRepository.save(endpoint2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/store", parentApi.getUuid())
                        .queryParam("name", "Test")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Store Endpoint')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Other Endpoint')]").doesNotExist());
        }
    }
}
