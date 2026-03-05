package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.model.ShoppingList;
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
class ShoppingListControllerTest {

    @Autowired
    private ShoppingListRepository shoppingListRepository;

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

        // Clean up any existing shopping lists
        shoppingListRepository.deleteAll();
    }

    @Nested
    class GetShoppingList {
        @Test
        void shouldReturnShoppingListWhenFound() throws Exception {
            // Create fresh test data
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ShoppingList testList = new ShoppingList();
            testList.setName("Test List 1");
            testList.setOwner("sub: testuser");
            testList.setRepeating(false);
            testList.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testList.setProductGroups(new HashMap<>());
            testList = shoppingListRepository.save(testList);
            
            mockMvc
                .perform(get("/api/shoppingLists/{uuid}", testList.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testList.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Test List 1"))
                .andExpect(jsonPath("$.repeating").value(false))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_1_UUID).value(2.5))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_2_UUID).value(1.0))
                .andExpect(jsonPath("$.productGroups").isMap())
                .andExpect(jsonPath("$.productGroups").isEmpty());
        }

        @Test
        void shouldReturn404WhenShoppingListNotFound() throws Exception {
            mockMvc
                .perform(get("/api/shoppingLists/{uuid}", Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAccessingShoppingListOfDifferentUser() throws Exception {
            // Create list for different user
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserList.setProductGroups(new HashMap<>());
            otherUserList = shoppingListRepository.save(otherUserList);

            mockMvc
                .perform(get("/api/shoppingLists/{uuid}", otherUserList.getUuid()).with(user_jwt))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CreateShoppingList {
        @Test
        void shouldCreateShoppingListWhenAuthorized() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            // Create canary list first
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            canaryList = shoppingListRepository.save(canaryList);

            String createJson = String.format(
                """
                    {
                      "name": "New List",
                      "repeating": false,
                      "products": {
                        "%s": 1.5,
                        "%s": 2.0
                      },
                      "productGroups": {}
                    }""", Testdata.PRODUCT_1_UUID, Testdata.PRODUCT_3_UUID
            );

            mockMvc
                .perform(post("/api/shoppingLists")
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("New List"))
                .andExpect(jsonPath("$.repeating").value(false))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_1_UUID).value(1.5))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_3_UUID).value(2.0))
                .andExpect(jsonPath("$.productGroups").isMap())
                .andExpect(jsonPath("$.productGroups").isEmpty());

            // Verify creation
            assertEquals(initialCount + 2, shoppingListRepository.count());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
            assertEquals(1,
                         unchangedCanary
                             .getProducts()
                             .size()
            );
        }
    }

    @Nested
    class UpdateShoppingList {
        @Test
        void shouldUpdateShoppingListWhenAuthorized() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            // Create test list
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();

            ShoppingList testList = new ShoppingList();
            testList.setName("Test List 1");
            testList.setOwner("sub: testuser");
            testList.setRepeating(false);
            testList.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testList.setProductGroups(new HashMap<>());
            testList = shoppingListRepository.save(testList);
            
            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            canaryList = shoppingListRepository.save(canaryList);

            String updateJson = String.format(
                """
                    {
                      "name": "Updated List 1",
                      "repeating": true,
                      "products": {
                        "%s": 4.0,
                        "%s": 2.5
                      },
                      "productGroups": {}
                    }""", Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID
            );

            mockMvc
                .perform(put("/api/shoppingLists/{uuid}", testList.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testList.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Updated List 1"))
                .andExpect(jsonPath("$.repeating").value(true))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_3_UUID).value(4.0))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_4_UUID).value(2.5))
                .andExpect(jsonPath("$.productGroups").isMap())
                .andExpect(jsonPath("$.productGroups").isEmpty());

            // Verify update and other lists unaffected
            assertEquals(initialCount + 2, shoppingListRepository.count());
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());

            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
        }
        
        @Test
        void shouldReturn404WhenUpdatingAnotherUsersShoppingList() throws Exception {
            // Create list for different user
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserList.setProductGroups(new HashMap<>());
            otherUserList = shoppingListRepository.save(otherUserList);
            
            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            canaryList = shoppingListRepository.save(canaryList);

            String updateJson = """
                {
                  "name": "Unauthorized Update",
                  "repeating": false,
                  "products": {},
                  "productGroups": {}
                }""";

            mockMvc
                .perform(put("/api/shoppingLists/{uuid}", otherUserList.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isNotFound());

            // Verify other user's list unchanged
            ShoppingList unchangedOtherList = shoppingListRepository
                .findById(otherUserList.getUuid())
                .orElseThrow();
            assertEquals("Other User List", unchangedOtherList.getName());
            assertFalse(unchangedOtherList.isRepeating());
            
            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
        }
    }

    @Nested
    class DeleteShoppingList {
        @Test
        void shouldDeleteShoppingListWhenAuthorized() throws Exception {
            // Create test list
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ShoppingList testList = new ShoppingList();
            testList.setName("Test List 1");
            testList.setOwner("sub: testuser");
            testList.setRepeating(false);
            testList.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testList.setProductGroups(new HashMap<>());
            testList = shoppingListRepository.save(testList);
            
            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            canaryList = shoppingListRepository.save(canaryList);
            
            long initialCount = shoppingListRepository.count();

            mockMvc
                .perform(delete("/api/shoppingLists/{uuid}", testList.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify deletion
            assertEquals(initialCount - 1, shoppingListRepository.count());
            assertFalse(shoppingListRepository
                            .findById(testList.getUuid())
                            .isPresent());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
        }
        
        @Test
        void shouldReturn404WhenDeletingAnotherUsersShoppingList() throws Exception {
            // Create list for different user
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserList.setProductGroups(new HashMap<>());
            otherUserList = shoppingListRepository.save(otherUserList);
            
            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            canaryList = shoppingListRepository.save(canaryList);
            
            long initialCount = shoppingListRepository.count();

            mockMvc
                .perform(delete("/api/shoppingLists/{uuid}", otherUserList.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify other user's list still exists and unchanged
            assertTrue(shoppingListRepository
                           .findById(otherUserList.getUuid())
                           .isPresent());
            ShoppingList unchangedOtherList = shoppingListRepository
                .findById(otherUserList.getUuid())
                .orElseThrow();
            assertEquals("Other User List", unchangedOtherList.getName());
            assertFalse(unchangedOtherList.isRepeating());
            
            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            assertEquals(initialCount, shoppingListRepository.count());
        }

        @Test
        void shouldDeleteNonRepeatingListWhenIfNonRepeatingIsTrue() throws Exception {
            // Create non-repeating test list
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingList nonRepeatingList = new ShoppingList();
            nonRepeatingList.setName("Non-Repeating List");
            nonRepeatingList.setOwner("sub: testuser");
            nonRepeatingList.setRepeating(false);
            nonRepeatingList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            nonRepeatingList.setProductGroups(new HashMap<>());
            nonRepeatingList = shoppingListRepository.save(nonRepeatingList);
            
            // Create repeating canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Repeating Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(true);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.0"))));
            canaryList.setProductGroups(new HashMap<>());
            canaryList = shoppingListRepository.save(canaryList);
            
            long initialCount = shoppingListRepository.count();

            mockMvc
                .perform(delete("/api/shoppingLists/{uuid}?ifNonRepeating=true", nonRepeatingList.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify deletion of non-repeating list
            assertEquals(initialCount - 1, shoppingListRepository.count());
            assertFalse(shoppingListRepository
                            .findById(nonRepeatingList.getUuid())
                            .isPresent());

            // Verify repeating canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Repeating Canary List", unchangedCanary.getName());
            assertTrue(unchangedCanary.isRepeating());
        }

        @Test
        void shouldNotDeleteRepeatingListWhenIfNonRepeatingIsTrue() throws Exception {
            // Create repeating test list
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingList repeatingList = new ShoppingList();
            repeatingList.setName("Repeating List");
            repeatingList.setOwner("sub: testuser");
            repeatingList.setRepeating(true);
            repeatingList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            repeatingList.setProductGroups(new HashMap<>());
            repeatingList = shoppingListRepository.save(repeatingList);
            
            // Create non-repeating canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Non-Repeating Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.0"))));
            canaryList.setProductGroups(new HashMap<>());
            canaryList = shoppingListRepository.save(canaryList);
            
            long initialCount = shoppingListRepository.count();

            mockMvc
                .perform(delete("/api/shoppingLists/{uuid}?ifNonRepeating=true", repeatingList.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify repeating list was NOT deleted
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository
                           .findById(repeatingList.getUuid())
                           .isPresent());

            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Non-Repeating Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
        }
    }

    @Nested
    class SearchShoppingLists {
        @Test
        void shouldReturnAllShoppingListsWhenSearchingWithEmptyName() throws Exception {
            // Create test lists
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            Product product3 = productRepository.findById(Testdata.PRODUCT_3_UUID).orElseThrow();

            ShoppingList testList1 = new ShoppingList();
            testList1.setName("Test List 1");
            testList1.setOwner("sub: testuser");
            testList1.setRepeating(false);
            testList1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            testList1.setProductGroups(new HashMap<>());
            shoppingListRepository.save(testList1);
            
            ShoppingList testList2 = new ShoppingList();
            testList2.setName("Test List 2");
            testList2.setOwner("sub: testuser");
            testList2.setRepeating(true);
            testList2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            testList2.setProductGroups(new HashMap<>());
            shoppingListRepository.save(testList2);
            
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.save(canaryList);
            
            mockMvc
                .perform(get("/api/shoppingLists").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test List 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Test List 2')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Canary List')]").exists());
            
            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
            assertEquals(1, unchangedCanary.getProducts().size());
        }

        @Test
        void shouldReturnFilteredShoppingListsWhenSearchingWithName() throws Exception {
            // Create test lists
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            Product product3 = productRepository.findById(Testdata.PRODUCT_3_UUID).orElseThrow();
            
            ShoppingList testList1 = new ShoppingList();
            testList1.setName("Test List 1");
            testList1.setOwner("sub: testuser");
            testList1.setRepeating(false);
            testList1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            testList1.setProductGroups(new HashMap<>());
            shoppingListRepository.save(testList1);
            
            ShoppingList testList2 = new ShoppingList();
            testList2.setName("Test List 2");
            testList2.setOwner("sub: testuser");
            testList2.setRepeating(true);
            testList2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            testList2.setProductGroups(new HashMap<>());
            shoppingListRepository.save(testList2);
            
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.save(canaryList);
            
            mockMvc
                .perform(get("/api/shoppingLists?name=Test").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test List 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Test List 2')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Canary List')]").doesNotExist());
            
            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
            assertEquals(1, unchangedCanary.getProducts().size());
        }

        @Test
        void shouldReturnEmptyResultWhenSearchingWithNonExistentName() throws Exception {
            mockMvc
                .perform(get("/api/shoppingLists?name=NonExistent").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        void shouldReturnOnlyUserOwnsShoppingLists() throws Exception {
            // Create test lists for current user
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ShoppingList testList1 = new ShoppingList();
            testList1.setName("Test List 1");
            testList1.setOwner("sub: testuser");
            testList1.setRepeating(false);
            testList1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            testList1.setProductGroups(new HashMap<>());
            shoppingListRepository.save(testList1);
            
            ShoppingList testList2 = new ShoppingList();
            testList2.setName("Test List 2");
            testList2.setOwner("sub: testuser");
            testList2.setRepeating(true);
            testList2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            testList2.setProductGroups(new HashMap<>());
            shoppingListRepository.save(testList2);
            
            // Create canary list
            ShoppingList canaryList = new ShoppingList();
            canaryList.setName("Canary List");
            canaryList.setOwner("sub: testuser");
            canaryList.setRepeating(false);
            canaryList.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryList.setProductGroups(new HashMap<>());
            shoppingListRepository.save(canaryList);
            
            // Create list for different user
            ShoppingList otherUserList = new ShoppingList();
            otherUserList.setName("Other User Test List");
            otherUserList.setOwner("sub: otheruser");
            otherUserList.setRepeating(false);
            otherUserList.setProducts(new HashMap<>());
            otherUserList.setProductGroups(new HashMap<>());
            shoppingListRepository.save(otherUserList);

            mockMvc
                .perform(get("/api/shoppingLists?name=Test").with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test List 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Test List 2')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Other User Test List')]").doesNotExist());
            
            // Verify canary list unaffected
            assertTrue(shoppingListRepository
                           .findById(canaryList.getUuid())
                           .isPresent());
            ShoppingList unchangedCanary = shoppingListRepository
                .findById(canaryList.getUuid())
                .get();
            assertEquals("Canary List", unchangedCanary.getName());
            assertFalse(unchangedCanary.isRepeating());
            assertEquals(1, unchangedCanary.getProducts().size());
        }
    }
}
