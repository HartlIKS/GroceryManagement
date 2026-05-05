package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.jpa.mdi.PriceEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import de.iks.grocery_manager.server.model.mdi.PriceEndpoint;
import de.iks.grocery_manager.server.model.mdi.ResponseType;
import de.iks.grocery_manager.server.model.mdi.handling.Parameter;
import de.iks.grocery_manager.server.model.mdi.handling.ProductHandlingType;
import de.iks.grocery_manager.server.model.mdi.handling.StoreHandlingType;
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

import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
class PriceEndpointControllerTest {
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

    private MockMvc mockMvc;

    @Autowired
    private PriceEndpointRepository priceEndpointRepository;

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
        priceEndpointRepository.deleteAll();

        // Create parent ExternalAPI
        parentApi = new ExternalAPI();
        parentApi.setName("Test API");
        parentApi.setProductMappings(new HashMap<>());
        parentApi.setStoreMappings(new HashMap<>());
        parentApi = externalAPIRepository.save(parentApi);
    }

    @Nested
    class GetPriceEndpoint {
        @Test
        void shouldReturnPriceEndpointWhenFound() throws Exception {
            // Create test data
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
            endpoint = priceEndpointRepository.save(endpoint);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/price/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(endpoint.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Test Price Endpoint"));
        }

        @Test
        void shouldReturn404WhenPriceEndpointNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/price/{uuid}", parentApi.getUuid(), Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdatePriceEndpoint {
        @Test
        void shouldUpdatePriceEndpointWhenAuthorizedAndFound() throws Exception {
            // Create test data
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
            endpoint = priceEndpointRepository.save(endpoint);

            long initialCount = priceEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/price/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .content(PRICE_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(endpoint.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Price Endpoint 1 Updated"));

            // Verify update was applied
            assertTrue(priceEndpointRepository.findById(endpoint.getUuid()).isPresent());
            assertEquals("Price Endpoint 1 Updated", priceEndpointRepository.findById(endpoint.getUuid()).get().getName());
            assertEquals(initialCount, priceEndpointRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentPriceEndpoint() throws Exception {
            long initialCount = priceEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/price/{uuid}", parentApi.getUuid(), Testdata.BAD_UUID)
                        .content(PRICE_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());

            // Verify no changes
            assertEquals(initialCount, priceEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenUpdatingPriceEndpointWithoutAuthorization() throws Exception {
            // Create test data
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
            endpoint = priceEndpointRepository.save(endpoint);

            long initialCount = priceEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/price/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .content(PRICE_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceEndpointRepository.count());
            assertEquals("Test Price Endpoint", priceEndpointRepository.findById(endpoint.getUuid()).get().getName());
        }
    }

    @Nested
    class CreatePriceEndpoint {
        @Test
        void shouldCreatePriceEndpointWhenAuthorized() throws Exception {
            long initialCount = priceEndpointRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface/{parentUuid}/endpoint/price", parentApi.getUuid())
                        .content(PRICE_ENDPOINT_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/masterdata/interface/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}/endpoint/price/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Price Endpoint 1"));

            // Verify creation
            assertEquals(initialCount + 1, priceEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenCreatingPriceEndpointWithoutAuthorization() throws Exception {
            long initialCount = priceEndpointRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface/{parentUuid}/endpoint/price", parentApi.getUuid())
                        .content(PRICE_ENDPOINT_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceEndpointRepository.count());
        }
    }

    @Nested
    class DeletePriceEndpoint {
        @Test
        void shouldDeletePriceEndpointWhenAuthorized() throws Exception {
            // Create test data
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
            endpoint = priceEndpointRepository.save(endpoint);

            long initialCount = priceEndpointRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{parentUuid}/endpoint/price/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());

            // Verify deletion
            assertFalse(priceEndpointRepository.findById(endpoint.getUuid()).isPresent());
            assertEquals(initialCount - 1, priceEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenDeletingPriceEndpointWithoutAuthorization() throws Exception {
            // Create test data
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
            endpoint = priceEndpointRepository.save(endpoint);

            long initialCount = priceEndpointRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{parentUuid}/endpoint/price/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, priceEndpointRepository.count());
            assertTrue(priceEndpointRepository.findById(endpoint.getUuid()).isPresent());
        }
    }

    @Nested
    class SearchPriceEndpoints {
        @Test
        void shouldReturnAllPriceEndpointsWhenSearching() throws Exception {
            // Create test data
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
            endpoint1 = priceEndpointRepository.save(endpoint1);

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
            endpoint2 = priceEndpointRepository.save(endpoint2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/price", parentApi.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Price Endpoint 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Price Endpoint 2')]").exists());
        }

        @Test
        void shouldReturnFilteredPriceEndpointsWhenSearchingByName() throws Exception {
            // Create test data
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
            endpoint1 = priceEndpointRepository.save(endpoint1);

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
            endpoint2 = priceEndpointRepository.save(endpoint2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/price", parentApi.getUuid())
                        .queryParam("name", "Test")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Price Endpoint')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Other Endpoint')]").doesNotExist());
        }
    }
}
