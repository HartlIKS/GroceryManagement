package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
class ProductControllerTest {
    private static final String PRODUCT_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Product 1"
        }""", Testdata.PRODUCT_1_UUID);
    private static final String PRODUCT_1_UPDATE_JSON = """
        {
          "name": "Product 1b",
          "EAN": "123456"
        }""";
    private static final String PRODUCT_3_JSON = """
        {
          "name": "Product 3",
          "EAN": "654321"
        }""";
    private static final String PRODUCT_3_CREATE_JSON = PRODUCT_3_JSON;

    private MockMvc mockMvc;
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuthorityConfiguration authorityConfiguration;

    private RequestPostProcessor admin_jwt;
    private final RequestPostProcessor user_jwt = jwt();

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        admin_jwt = jwt()
            .authorities(new SimpleGrantedAuthority(authorityConfiguration.getMasterdataAuthority()));
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();
    }

    @Nested
    class GetProduct {
        @Test
        void shouldReturnProductWhenFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PRODUCT_1_JSON));
        }

        @Test
        void shouldReturn404WhenProductNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/product/{uuid}", Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateProduct {
        @Test
        void shouldUpdateProductWhenAuthorizedAndFound() throws Exception {
            long initialCount = productRepository.count();
            
            mockMvc
                .perform(
                    put("/api/masterdata/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .content(PRODUCT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "uuid": "%s",
                      "name": "Product 1b",
                      "EAN": "123456"
                    }""", Testdata.PRODUCT_1_UUID)));
            
            // Verify update was applied and other product unaffected
            assertTrue(productRepository.findById(Testdata.PRODUCT_1_UUID).isPresent());
            assertTrue(productRepository.findById(Testdata.PRODUCT_2_UUID).isPresent());
            assertEquals(initialCount, productRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            long initialCount = productRepository.count();
            
            mockMvc
                .perform(
                    put("/api/masterdata/product/{uuid}", Testdata.BAD_UUID)
                        .content(PRODUCT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
            
            // Verify no changes to existing products
            assertTrue(productRepository.findById(Testdata.PRODUCT_1_UUID).isPresent());
            assertTrue(productRepository.findById(Testdata.PRODUCT_2_UUID).isPresent());
            assertEquals(initialCount, productRepository.count());
        }

        @Test
        void shouldReturn403WhenUpdatingProductWithoutAuthorization() throws Exception {
            long initialCount = productRepository.count();
            
            mockMvc
                .perform(
                    put("/api/masterdata/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .content(PRODUCT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertTrue(productRepository.findById(Testdata.PRODUCT_1_UUID).isPresent());
            assertTrue(productRepository.findById(Testdata.PRODUCT_2_UUID).isPresent());
            assertEquals(initialCount, productRepository.count());
        }
    }

    @Nested
    class CreateProduct {
        @Test
        void shouldCreateProductWhenAuthorized() throws Exception {
            long initialCount = productRepository.count();
            
            mockMvc
                .perform(
                    post("/api/masterdata/product")
                        .content(PRODUCT_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/product/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PRODUCT_3_JSON));
            
            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, productRepository.count());
            // Verify existing products unaffected
            assertTrue(productRepository.findById(Testdata.PRODUCT_1_UUID).isPresent());
            assertTrue(productRepository.findById(Testdata.PRODUCT_2_UUID).isPresent());
        }

        @Test
        void shouldReturn403WhenCreatingProductWithoutAuthorization() throws Exception {
            long initialCount = productRepository.count();
            
            mockMvc
                .perform(
                    post("/api/masterdata/product")
                        .content(PRODUCT_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, productRepository.count());
            assertTrue(productRepository.findById(Testdata.PRODUCT_1_UUID).isPresent());
            assertTrue(productRepository.findById(Testdata.PRODUCT_2_UUID).isPresent());
        }
    }

    @Nested
    class DeleteProduct {
        @Test
        void shouldDeleteProductWhenAuthorized() throws Exception {
            long initialCount = productRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/masterdata/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion
            assertFalse(productRepository.findById(Testdata.PRODUCT_1_UUID).isPresent());
            assertEquals(initialCount - 1, productRepository.count());
            // Verify other product unaffected
            assertTrue(productRepository.findById(Testdata.PRODUCT_2_UUID).isPresent());
        }

        @Test
        void shouldReturn403WhenDeletingProductWithoutAuthorization() throws Exception {
            long initialCount = productRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/masterdata/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, productRepository.count());
            assertTrue(productRepository.findById(Testdata.PRODUCT_1_UUID).isPresent());
            assertTrue(productRepository.findById(Testdata.PRODUCT_2_UUID).isPresent());
        }
    }

    @Nested
    class SearchProducts {
        @Test
        void shouldReturnAllProductsWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/product")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "page": {
                        "number": 0,
                        "size": 10,
                        "totalElements": 4,
                        "totalPages": 1
                      },
                      "content": [
                        %s,
                        {
                          "uuid": "%s",
                          "name": "Product 2"
                        },
                        {
                          "uuid": "%s",
                          "name": "Product 3"
                        },
                        {
                          "uuid": "%s",
                          "name": "Product 4"
                        }
                      ]
                    }""", PRODUCT_1_JSON, Testdata.PRODUCT_2_UUID, Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID
                )));
        }
    }
}