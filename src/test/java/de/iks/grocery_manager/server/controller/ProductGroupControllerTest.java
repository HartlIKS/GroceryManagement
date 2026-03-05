package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.model.ProductGroup;
import de.iks.grocery_manager.server.model.masterdata.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
class ProductGroupControllerTest {

    @Autowired
    private ProductGroupRepository productGroupRepository;

    @Autowired
    private ProductRepository productRepository;

    private MockMvc mockMvc;

    private final RequestPostProcessor user_jwt = jwt()
        .jwt(j -> j.subject("testuser"));

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();

        // Clean up any existing product groups
        productGroupRepository.deleteAll();
    }

    @Nested
    class GetProductGroup {
        @Test
        void shouldReturnProductGroupWhenFound() throws Exception {
            // Create fresh test data
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ProductGroup testGroup = new ProductGroup();
            testGroup.setName("Test Group 1");
            testGroup.setOwner("sub: testuser");
            testGroup.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testGroup = productGroupRepository.save(testGroup);
            
            mockMvc
                .perform(get("/api/productGroups/{uuid}", testGroup.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testGroup.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Test Group 1"))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_1_UUID).value(2.5))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_2_UUID).value(1.0));
        }

        @Test
        void shouldReturn404WhenProductGroupNotFound() throws Exception {
            mockMvc
                .perform(get("/api/productGroups/{uuid}", Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAccessingProductGroupOfDifferentUser() throws Exception {
            // Create group for different user
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>());
            otherUserGroup = productGroupRepository.save(otherUserGroup);

            mockMvc
                .perform(get("/api/productGroups/{uuid}", otherUserGroup.getUuid()).with(user_jwt))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CreateProductGroup {
        @Test
        void shouldCreateProductGroupWhenAuthorized() throws Exception {
            long initialCount = productGroupRepository.count();
            
            // Create canary group first
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryGroup = productGroupRepository.save(canaryGroup);

            String createJson = String.format(
                """
                    {
                      "name": "New Group",
                      "products": {
                        "%s": 1.5,
                        "%s": 2.0
                      }
                    }""", Testdata.PRODUCT_1_UUID, Testdata.PRODUCT_3_UUID
            );

            mockMvc
                .perform(post("/api/productGroups")
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New Group"))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_1_UUID).value(1.5))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_3_UUID).value(2.0));

            // Verify creation
            assertEquals(initialCount + 2, productGroupRepository.count());

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findById(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
            assertEquals(1,
                         unchangedCanary
                             .getProducts()
                             .size()
            );
        }
    }

    @Nested
    class UpdateProductGroup {
        @Test
        void shouldUpdateProductGroupWhenAuthorized() throws Exception {
            long initialCount = productGroupRepository.count();
            
            // Create test group
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();

            ProductGroup testGroup = new ProductGroup();
            testGroup.setName("Test Group 1");
            testGroup.setOwner("sub: testuser");
            testGroup.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testGroup = productGroupRepository.save(testGroup);
            
            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryGroup = productGroupRepository.save(canaryGroup);

            String updateJson = String.format(
                """
                    {
                      "name": "Updated Group 1",
                      "products": {
                        "%s": 4.0,
                        "%s": 2.5
                      }
                    }""", Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID
            );

            mockMvc
                .perform(put("/api/productGroups/{uuid}", testGroup.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testGroup.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Updated Group 1"))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_3_UUID).value(4.0))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_4_UUID).value(2.5));

            // Verify update and other groups unaffected
            assertEquals(initialCount + 2, productGroupRepository.count());
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());

            ProductGroup unchangedCanary = productGroupRepository
                .findById(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
        }
        
        @Test
        void shouldReturn404WhenUpdatingAnotherUsersProductGroup() throws Exception {
            // Create group for different user
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserGroup = productGroupRepository.save(otherUserGroup);
            
            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryGroup = productGroupRepository.save(canaryGroup);

            String updateJson = """
                {
                  "name": "Unauthorized Update",
                  "products": {}
                }""";

            mockMvc
                .perform(put("/api/productGroups/{uuid}", otherUserGroup.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isNotFound());

            // Verify other user's group unchanged
            ProductGroup unchangedOtherGroup = productGroupRepository
                .findById(otherUserGroup.getUuid())
                .orElseThrow();
            assertEquals("Other User Group", unchangedOtherGroup.getName());
            
            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findById(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
        }
    }

    @Nested
    class DeleteProductGroup {
        @Test
        void shouldDeleteProductGroupWhenAuthorized() throws Exception {
            // Create test group
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ProductGroup testGroup = new ProductGroup();
            testGroup.setName("Test Group 1");
            testGroup.setOwner("sub: testuser");
            testGroup.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testGroup = productGroupRepository.save(testGroup);
            
            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryGroup = productGroupRepository.save(canaryGroup);
            
            long initialCount = productGroupRepository.count();

            mockMvc
                .perform(delete("/api/productGroups/{uuid}", testGroup.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify deletion
            assertEquals(initialCount - 1, productGroupRepository.count());
            assertFalse(productGroupRepository
                            .findById(testGroup.getUuid())
                            .isPresent());

            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());
        }
        
        @Test
        void shouldReturn404WhenDeletingAnotherUsersProductGroup() throws Exception {
            // Create group for different user
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserGroup = productGroupRepository.save(otherUserGroup);
            
            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryGroup = productGroupRepository.save(canaryGroup);
            
            long initialCount = productGroupRepository.count();

            mockMvc
                .perform(delete("/api/productGroups/{uuid}", otherUserGroup.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify other user's group still exists and unchanged
            assertTrue(productGroupRepository
                           .findById(otherUserGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedOtherGroup = productGroupRepository
                .findById(otherUserGroup.getUuid())
                .orElseThrow();
            assertEquals("Other User Group", unchangedOtherGroup.getName());
            
            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }
    }

    @Nested
    class SearchProductGroups {
        @Test
        void shouldReturnAllProductGroupsWhenSearchingWithEmptyName() throws Exception {
            // Create test groups
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            Product product3 = productRepository.findById(Testdata.PRODUCT_3_UUID).orElseThrow();

            ProductGroup testGroup1 = new ProductGroup();
            testGroup1.setName("Test Group 1");
            testGroup1.setOwner("sub: testuser");
            testGroup1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            productGroupRepository.save(testGroup1);
            
            ProductGroup testGroup2 = new ProductGroup();
            testGroup2.setName("Test Group 2");
            testGroup2.setOwner("sub: testuser");
            testGroup2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            productGroupRepository.save(testGroup2);
            
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            productGroupRepository.save(canaryGroup);
            
            mockMvc
                .perform(get("/api/productGroups").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Group 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Group 2')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Canary Group')]").exists());
            
            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findById(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
            assertEquals(1, unchangedCanary.getProducts().size());
        }

        @Test
        void shouldReturnFilteredProductGroupsWhenSearchingWithName() throws Exception {
            // Create test groups
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            Product product3 = productRepository.findById(Testdata.PRODUCT_3_UUID).orElseThrow();
            
            ProductGroup testGroup1 = new ProductGroup();
            testGroup1.setName("Test Group 1");
            testGroup1.setOwner("sub: testuser");
            testGroup1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            productGroupRepository.save(testGroup1);
            
            ProductGroup testGroup2 = new ProductGroup();
            testGroup2.setName("Test Group 2");
            testGroup2.setOwner("sub: testuser");
            testGroup2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            productGroupRepository.save(testGroup2);
            
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            productGroupRepository.save(canaryGroup);
            
            mockMvc
                .perform(get("/api/productGroups?name=Test").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Group 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Group 2')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Canary Group')]").doesNotExist());
            
            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findById(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
            assertEquals(1, unchangedCanary.getProducts().size());
        }

        @Test
        void shouldReturnEmptyResultWhenSearchingWithNonExistentName() throws Exception {
            mockMvc
                .perform(get("/api/productGroups?name=NonExistent").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        void shouldReturnOnlyUserOwnsProductGroups() throws Exception {
            // Create test groups for current user
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ProductGroup testGroup1 = new ProductGroup();
            testGroup1.setName("Test Group 1");
            testGroup1.setOwner("sub: testuser");
            testGroup1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            productGroupRepository.save(testGroup1);
            
            ProductGroup testGroup2 = new ProductGroup();
            testGroup2.setName("Test Group 2");
            testGroup2.setOwner("sub: testuser");
            testGroup2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            productGroupRepository.save(testGroup2);
            
            // Create canary group
            ProductGroup canaryGroup = new ProductGroup();
            canaryGroup.setName("Canary Group");
            canaryGroup.setOwner("sub: testuser");
            canaryGroup.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            productGroupRepository.save(canaryGroup);
            
            // Create group for different user
            ProductGroup otherUserGroup = new ProductGroup();
            otherUserGroup.setName("Other User Test Group");
            otherUserGroup.setOwner("sub: otheruser");
            otherUserGroup.setProducts(new HashMap<>());
            productGroupRepository.save(otherUserGroup);

            mockMvc
                .perform(get("/api/productGroups?name=Test").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Group 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Test Group 2')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Other User Test Group')]").doesNotExist());
            
            // Verify canary group unaffected
            assertTrue(productGroupRepository
                           .findById(canaryGroup.getUuid())
                           .isPresent());
            ProductGroup unchangedCanary = productGroupRepository
                .findById(canaryGroup.getUuid())
                .get();
            assertEquals("Canary Group", unchangedCanary.getName());
            assertEquals(1, unchangedCanary.getProducts().size());
        }
    }
}
