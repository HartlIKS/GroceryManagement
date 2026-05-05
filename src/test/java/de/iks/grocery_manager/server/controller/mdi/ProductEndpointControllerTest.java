package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.jpa.mdi.ProductEndpointRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import de.iks.grocery_manager.server.model.mdi.ProductEndpoint;
import de.iks.grocery_manager.server.model.mdi.ResponseType;
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
class ProductEndpointControllerTest {
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

    private MockMvc mockMvc;

    @Autowired
    private ProductEndpointRepository productEndpointRepository;

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
        productEndpointRepository.deleteAll();

        // Create parent ExternalAPI
        parentApi = new ExternalAPI();
        parentApi.setName("Test API");
        parentApi.setProductMappings(new HashMap<>());
        parentApi.setStoreMappings(new HashMap<>());
        parentApi = externalAPIRepository.save(parentApi);
    }

    @Nested
    class GetProductEndpoint {
        @Test
        void shouldReturnProductEndpointWhenFound() throws Exception {
            // Create test data
            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            endpoint = productEndpointRepository.save(endpoint);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/product/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(endpoint.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Test Product Endpoint"));
        }

        @Test
        void shouldReturn404WhenProductEndpointNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/product/{uuid}", parentApi.getUuid(), Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateProductEndpoint {
        @Test
        void shouldUpdateProductEndpointWhenAuthorizedAndFound() throws Exception {
            // Create test data
            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            endpoint = productEndpointRepository.save(endpoint);

            long initialCount = productEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/product/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .content(PRODUCT_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(endpoint.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Product Endpoint 1 Updated"));

            // Verify update was applied
            assertTrue(productEndpointRepository.findById(endpoint.getUuid()).isPresent());
            assertEquals("Product Endpoint 1 Updated", productEndpointRepository.findById(endpoint.getUuid()).get().getName());
            assertEquals(initialCount, productEndpointRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentProductEndpoint() throws Exception {
            long initialCount = productEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/product/{uuid}", parentApi.getUuid(), Testdata.BAD_UUID)
                        .content(PRODUCT_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());

            // Verify no changes
            assertEquals(initialCount, productEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenUpdatingProductEndpointWithoutAuthorization() throws Exception {
            // Create test data
            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            endpoint = productEndpointRepository.save(endpoint);

            long initialCount = productEndpointRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{parentUuid}/endpoint/product/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .content(PRODUCT_ENDPOINT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, productEndpointRepository.count());
            assertEquals("Test Product Endpoint", productEndpointRepository.findById(endpoint.getUuid()).get().getName());
        }
    }

    @Nested
    class CreateProductEndpoint {
        @Test
        void shouldCreateProductEndpointWhenAuthorized() throws Exception {
            long initialCount = productEndpointRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface/{parentUuid}/endpoint/product", parentApi.getUuid())
                        .content(PRODUCT_ENDPOINT_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/masterdata/interface/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}/endpoint/product/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("Product Endpoint 1"));

            // Verify creation
            assertEquals(initialCount + 1, productEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenCreatingProductEndpointWithoutAuthorization() throws Exception {
            long initialCount = productEndpointRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface/{parentUuid}/endpoint/product", parentApi.getUuid())
                        .content(PRODUCT_ENDPOINT_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, productEndpointRepository.count());
        }
    }

    @Nested
    class DeleteProductEndpoint {
        @Test
        void shouldDeleteProductEndpointWhenAuthorized() throws Exception {
            // Create test data
            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            endpoint = productEndpointRepository.save(endpoint);

            long initialCount = productEndpointRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{parentUuid}/endpoint/product/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());

            // Verify deletion
            assertFalse(productEndpointRepository.findById(endpoint.getUuid()).isPresent());
            assertEquals(initialCount - 1, productEndpointRepository.count());
        }

        @Test
        void shouldReturn403WhenDeletingProductEndpointWithoutAuthorization() throws Exception {
            // Create test data
            ProductEndpoint endpoint = new ProductEndpoint();
            endpoint.setApi(parentApi);
            endpoint.setName("Test Product Endpoint");
            endpoint.setBaseUrl("https://api.example.com");
            endpoint.setBasePath("/products");
            endpoint.setProductIdPath("$.id");
            endpoint.setResponseType(ResponseType.JSON);
            endpoint = productEndpointRepository.save(endpoint);

            long initialCount = productEndpointRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{parentUuid}/endpoint/product/{uuid}", parentApi.getUuid(), endpoint.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, productEndpointRepository.count());
            assertTrue(productEndpointRepository.findById(endpoint.getUuid()).isPresent());
        }
    }

    @Nested
    class SearchProductEndpoints {
        @Test
        void shouldReturnAllProductEndpointsWhenSearching() throws Exception {
            // Create test data
            ProductEndpoint endpoint1 = new ProductEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Product Endpoint 1");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/products");
            endpoint1.setProductIdPath("$.id");
            endpoint1.setResponseType(ResponseType.JSON);
            endpoint1 = productEndpointRepository.save(endpoint1);

            ProductEndpoint endpoint2 = new ProductEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Product Endpoint 2");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/products");
            endpoint2.setProductIdPath("$.id");
            endpoint2.setResponseType(ResponseType.JSON);
            endpoint2 = productEndpointRepository.save(endpoint2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/product", parentApi.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Product Endpoint 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Product Endpoint 2')]").exists());
        }

        @Test
        void shouldReturnFilteredProductEndpointsWhenSearchingByName() throws Exception {
            // Create test data
            ProductEndpoint endpoint1 = new ProductEndpoint();
            endpoint1.setApi(parentApi);
            endpoint1.setName("Test Product Endpoint");
            endpoint1.setBaseUrl("https://api.example.com");
            endpoint1.setBasePath("/products");
            endpoint1.setProductIdPath("$.id");
            endpoint1.setResponseType(ResponseType.JSON);
            endpoint1 = productEndpointRepository.save(endpoint1);

            ProductEndpoint endpoint2 = new ProductEndpoint();
            endpoint2.setApi(parentApi);
            endpoint2.setName("Other Endpoint");
            endpoint2.setBaseUrl("https://api.example.com");
            endpoint2.setBasePath("/products");
            endpoint2.setProductIdPath("$.id");
            endpoint2.setResponseType(ResponseType.JSON);
            endpoint2 = productEndpointRepository.save(endpoint2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{parentUuid}/endpoint/product", parentApi.getUuid())
                        .queryParam("name", "Test")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Product Endpoint')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Other Endpoint')]").doesNotExist());
        }
    }
}
