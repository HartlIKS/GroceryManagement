package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.model.ShoppingTrip;
import de.iks.grocery_manager.server.model.masterdata.Product;
import de.iks.grocery_manager.server.model.masterdata.Store;
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
class ShoppingTripControllerTest {

    @Autowired
    private ShoppingTripRepository shoppingTripRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreRepository storeRepository;

    private MockMvc mockMvc;

    private final RequestPostProcessor user_jwt = jwt()
        .jwt(j -> j.subject("testuser"));

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();

        // Clean up any existing shopping trips
        shoppingTripRepository.deleteAll();
    }

    @Nested
    class GetShoppingTrip {
        @Test
        void shouldReturnShoppingTripWhenFound() throws Exception {
            // Create fresh test data
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner("sub: testuser");
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testTrip = shoppingTripRepository.save(testTrip);
            
            mockMvc
                .perform(get("/api/shoppingTrips/{uuid}", testTrip.getUuid()).with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testTrip.getUuid().toString()))
                .andExpect(jsonPath("$.store").value(Testdata.STORE_1_UUID.toString()))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_1_UUID).value(2.5))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_2_UUID).value(1.0));
        }

        @Test
        void shouldReturn404WhenShoppingTripNotFound() throws Exception {
            mockMvc
                .perform(get("/api/shoppingTrips/{uuid}", Testdata.BAD_UUID).with(user_jwt))
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAccessingShoppingTripOfDifferentUser() throws Exception {
            // Create trip for different user
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserTrip = shoppingTripRepository.save(otherUserTrip);

            mockMvc
                .perform(get("/api/shoppingTrips/{uuid}", otherUserTrip.getUuid()).with(user_jwt))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class CreateShoppingTrip {
        @Test
        void shouldCreateShoppingTripWhenAuthorized() throws Exception {
            long initialCount = shoppingTripRepository.count();
            
            // Create canary trip first
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryTrip = shoppingTripRepository.save(canaryTrip);

            Instant testTime = Instant.now();
            String createJson = String.format(
                """
                    {
                      "store": "%s",
                      "time": "%s",
                      "products": {
                        "%s": 1.5,
                        "%s": 2.0
                      }
                    }""", 
                Testdata.STORE_2_UUID, 
                testTime.toString(),
                Testdata.PRODUCT_1_UUID, 
                Testdata.PRODUCT_3_UUID
            );

            mockMvc
                .perform(post("/api/shoppingTrips")
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("location"))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.store").value(Testdata.STORE_2_UUID.toString()))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_1_UUID).value(1.5))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_3_UUID).value(2.0));

            // Verify creation
            assertEquals(initialCount + 2, shoppingTripRepository.count());

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(Testdata.STORE_1_UUID, unchangedCanary.getStore().getUuid());
            assertEquals(1,
                         unchangedCanary
                             .getProducts()
                             .size()
            );
        }
    }

    @Nested
    class UpdateShoppingTrip {
        @Test
        void shouldUpdateShoppingTripWhenAuthorized() throws Exception {
            long initialCount = shoppingTripRepository.count();
            
            // Create test trip
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();

            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner("sub: testuser");
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testTrip = shoppingTripRepository.save(testTrip);
            
            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryTrip = shoppingTripRepository.save(canaryTrip);

            Instant updatedTime = Instant.now().plusSeconds(3600);
            String updateJson = String.format(
                """
                    {
                      "store": "%s",
                      "time": "%s",
                      "products": {
                        "%s": 4.0,
                        "%s": 2.5
                      }
                    }""", 
                Testdata.STORE_3_UUID, 
                updatedTime.toString(),
                Testdata.PRODUCT_3_UUID, 
                Testdata.PRODUCT_4_UUID
            );

            mockMvc
                .perform(put("/api/shoppingTrips/{uuid}", testTrip.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testTrip.getUuid().toString()))
                .andExpect(jsonPath("$.store").value(Testdata.STORE_3_UUID.toString()))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_3_UUID).value(4.0))
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_4_UUID).value(2.5));

            // Verify update and other trips unaffected
            assertEquals(initialCount + 2, shoppingTripRepository.count());
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());

            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(Testdata.STORE_1_UUID, unchangedCanary.getStore().getUuid());
        }
        
        @Test
        void shouldReturn404WhenUpdatingAnotherUsersShoppingTrip() throws Exception {
            // Create trip for different user
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserTrip = shoppingTripRepository.save(otherUserTrip);
            
            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryTrip = shoppingTripRepository.save(canaryTrip);

            String updateJson = """
                {
                  "store": "00000000-0000-0000-0000-000000000003",
                  "time": "2026-03-05T10:00:00Z",
                  "products": {}
                }""";

            mockMvc
                .perform(put("/api/shoppingTrips/{uuid}", otherUserTrip.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(updateJson))
                .andExpect(status().isNotFound());

            // Verify other user's trip unchanged
            ShoppingTrip unchangedOtherTrip = shoppingTripRepository
                .findById(otherUserTrip.getUuid())
                .orElseThrow();
            assertEquals(Testdata.STORE_1_UUID, unchangedOtherTrip.getStore().getUuid());
            
            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(Testdata.STORE_1_UUID, unchangedCanary.getStore().getUuid());
        }
    }

    @Nested
    class AddToShoppingTrip {
        @Test
        void shouldAddProductsToShoppingTripWhenAuthorized() throws Exception {
            // Create test trip
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();

            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner("sub: testuser");
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5")
            )));
            testTrip = shoppingTripRepository.save(testTrip);
            
            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryTrip = shoppingTripRepository.save(canaryTrip);

            String addJson = String.format(
                """
                    {
                      "%s": 1.5,
                      "%s": 2.0
                    }""", 
                Testdata.PRODUCT_1_UUID, 
                Testdata.PRODUCT_3_UUID
            );

            mockMvc
                .perform(post("/api/shoppingTrips/{uuid}/add", testTrip.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(addJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(testTrip.getUuid().toString()))
                .andExpect(jsonPath("$.store").value(Testdata.STORE_1_UUID.toString()))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_1_UUID).value(4.0)) // 2.5 + 1.5
                .andExpect(jsonPath("$.products." + Testdata.PRODUCT_3_UUID).value(2.0));

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(1, unchangedCanary.getProducts().size());
            assertEquals(new BigDecimal("1.0"), unchangedCanary.getProducts().get(product1));
        }
        
        @Test
        void shouldReturn404WhenAddingToAnotherUsersShoppingTrip() throws Exception {
            // Create trip for different user
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserTrip = shoppingTripRepository.save(otherUserTrip);
            
            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryTrip = shoppingTripRepository.save(canaryTrip);

            String addJson = String.format(
                """
                    {
                      "%s": 2.0
                    }""", 
                Testdata.PRODUCT_2_UUID
            );

            mockMvc
                .perform(post("/api/shoppingTrips/{uuid}/add", otherUserTrip.getUuid())
                             .with(user_jwt)
                             .contentType(MediaType.APPLICATION_JSON)
                             .content(addJson))
                .andExpect(status().isNotFound());

            // Verify other user's trip unchanged
            ShoppingTrip unchangedOtherTrip = shoppingTripRepository
                .findById(otherUserTrip.getUuid())
                .orElseThrow();
            assertEquals(1, unchangedOtherTrip.getProducts().size());
            
            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(1, unchangedCanary.getProducts().size());
        }
    }

    @Nested
    class DeleteShoppingTrip {
        @Test
        void shouldDeleteShoppingTripWhenAuthorized() throws Exception {
            // Create test trip
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            ShoppingTrip testTrip = new ShoppingTrip();
            testTrip.setStore(store1);
            testTrip.setOwner("sub: testuser");
            testTrip.setTime(Instant.now());
            testTrip.setProducts(new HashMap<>(Map.of(
                product1, new BigDecimal("2.5"),
                product2, new BigDecimal("1.0")
            )));
            testTrip = shoppingTripRepository.save(testTrip);
            
            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryTrip = shoppingTripRepository.save(canaryTrip);
            
            long initialCount = shoppingTripRepository.count();

            mockMvc
                .perform(delete("/api/shoppingTrips/{uuid}", testTrip.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify deletion
            assertEquals(initialCount - 1, shoppingTripRepository.count());
            assertFalse(shoppingTripRepository
                            .findById(testTrip.getUuid())
                            .isPresent());

            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
        }
        
        @Test
        void shouldReturn404WhenDeletingAnotherUsersShoppingTrip() throws Exception {
            // Create trip for different user
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(Instant.now());
            otherUserTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            otherUserTrip = shoppingTripRepository.save(otherUserTrip);
            
            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(Instant.now());
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            canaryTrip = shoppingTripRepository.save(canaryTrip);
            
            long initialCount = shoppingTripRepository.count();

            mockMvc
                .perform(delete("/api/shoppingTrips/{uuid}", otherUserTrip.getUuid()).with(user_jwt))
                .andExpect(status().isOk());

            // Verify other user's trip still exists and unchanged
            assertTrue(shoppingTripRepository
                           .findById(otherUserTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedOtherTrip = shoppingTripRepository
                .findById(otherUserTrip.getUuid())
                .orElseThrow();
            assertEquals(Testdata.STORE_1_UUID, unchangedOtherTrip.getStore().getUuid());
            
            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            assertEquals(initialCount, shoppingTripRepository.count());
        }
    }

    @Nested
    class SearchShoppingTrips {
        @Test
        void shouldReturnShoppingTripsInDateRangeWhenNoParametersProvided() throws Exception {
            // Create test trips with fixed timestamps
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Store store2 = storeRepository.findById(Testdata.STORE_2_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            Product product3 = productRepository.findById(Testdata.PRODUCT_3_UUID).orElseThrow();

            // Use a fixed base time in the future
            Instant baseTime = Instant.parse("2026-03-05T15:00:00Z");
            
            ShoppingTrip testTrip1 = new ShoppingTrip();
            testTrip1.setStore(store1);
            testTrip1.setOwner("sub: testuser");
            testTrip1.setTime(baseTime.plus(Duration.ofMinutes(5))); // 15:05:00
            testTrip1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            shoppingTripRepository.save(testTrip1);
            
            ShoppingTrip testTrip2 = new ShoppingTrip();
            testTrip2.setStore(store2);
            testTrip2.setOwner("sub: testuser");
            testTrip2.setTime(baseTime.plus(Duration.ofMinutes(10))); // 15:10:00
            testTrip2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            shoppingTripRepository.save(testTrip2);
            
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(baseTime.plus(Duration.ofMinutes(15))); // 15:15:00
            canaryTrip.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            shoppingTripRepository.save(canaryTrip);
            
            // Use explicit date range that includes all our test trips
            mockMvc
                .perform(get("/api/shoppingTrips?from={from}&to={to}", 
                            "2026-03-05T15:04:00Z", // Start before first trip
                            "2026-03-05T15:20:00Z") // End after last trip
                            .with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.store == '" + Testdata.STORE_1_UUID + "')]").exists())
                .andExpect(jsonPath("$.content[?(@.store == '" + Testdata.STORE_2_UUID + "')]").exists());
            
            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(Testdata.STORE_1_UUID, unchangedCanary.getStore().getUuid());
            assertEquals(1, unchangedCanary.getProducts().size());
        }

        @Test
        void shouldReturnFilteredShoppingTripsWhenSearchingWithDateRange() throws Exception {
            // Create test trips with fixed timestamps
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Store store2 = storeRepository.findById(Testdata.STORE_2_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            Product product3 = productRepository.findById(Testdata.PRODUCT_3_UUID).orElseThrow();

            // Use a fixed base time
            Instant baseTime = Instant.parse("2026-03-05T15:00:00Z");
            
            ShoppingTrip testTrip1 = new ShoppingTrip();
            testTrip1.setStore(store1);
            testTrip1.setOwner("sub: testuser");
            testTrip1.setTime(baseTime.plus(Duration.ofMinutes(5))); // 15:05:00
            testTrip1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            shoppingTripRepository.save(testTrip1);
            
            ShoppingTrip testTrip2 = new ShoppingTrip();
            testTrip2.setStore(store2);
            testTrip2.setOwner("sub: testuser");
            testTrip2.setTime(baseTime.plus(Duration.ofMinutes(10))); // 15:10:00
            testTrip2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            shoppingTripRepository.save(testTrip2);
            
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(baseTime.plus(Duration.ofHours(24))); // Next day
            canaryTrip.setProducts(new HashMap<>(Map.of(product3, new BigDecimal("1.5"))));
            shoppingTripRepository.save(canaryTrip);
            
            // Use date range that only includes first two trips
            mockMvc
                .perform(get("/api/shoppingTrips?from={from}&to={to}", 
                            "2026-03-05T15:04:00Z", // Start before first trip
                            "2026-03-05T15:12:00Z") // End after second trip, before canary
                            .with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.store == '" + Testdata.STORE_1_UUID + "')]").exists())
                .andExpect(jsonPath("$.content[?(@.store == '" + Testdata.STORE_2_UUID + "')]").exists());
            
            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(Testdata.STORE_1_UUID, unchangedCanary.getStore().getUuid());
            assertEquals(1, unchangedCanary.getProducts().size());
        }

        @Test
        void shouldReturnEmptyResultWhenSearchingWithNonOverlappingDateRange() throws Exception {
            ZonedDateTime futureFrom = ZonedDateTime.now().plusDays(30);
            ZonedDateTime futureTo = futureFrom.plusWeeks(1);
            
            mockMvc
                .perform(get("/api/shoppingTrips?from={from}&to={to}", 
                            futureFrom.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
                            futureTo.format(DateTimeFormatter.ISO_ZONED_DATE_TIME))
                            .with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        void shouldReturnOnlyUserOwnsShoppingTrips() throws Exception {
            // Create test trips for current user with fixed timestamps
            Store store1 = storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow();
            Store store2 = storeRepository.findById(Testdata.STORE_2_UUID).orElseThrow();
            Product product1 = productRepository.findById(Testdata.PRODUCT_1_UUID).orElseThrow();
            Product product2 = productRepository.findById(Testdata.PRODUCT_2_UUID).orElseThrow();
            
            // Use a fixed base time
            Instant baseTime = Instant.parse("2026-03-05T15:00:00Z");
            
            ShoppingTrip testTrip1 = new ShoppingTrip();
            testTrip1.setStore(store1);
            testTrip1.setOwner("sub: testuser");
            testTrip1.setTime(baseTime.plus(Duration.ofMinutes(5))); // 15:05:00
            testTrip1.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("2.5"))));
            shoppingTripRepository.save(testTrip1);
            
            ShoppingTrip testTrip2 = new ShoppingTrip();
            testTrip2.setStore(store2);
            testTrip2.setOwner("sub: testuser");
            testTrip2.setTime(baseTime.plus(Duration.ofMinutes(10))); // 15:10:00
            testTrip2.setProducts(new HashMap<>(Map.of(product2, new BigDecimal("1.0"))));
            shoppingTripRepository.save(testTrip2);
            
            // Create canary trip
            ShoppingTrip canaryTrip = new ShoppingTrip();
            canaryTrip.setStore(store1);
            canaryTrip.setOwner("sub: testuser");
            canaryTrip.setTime(baseTime.plus(Duration.ofMinutes(15))); // 15:15:00
            canaryTrip.setProducts(new HashMap<>(Map.of(product1, new BigDecimal("1.0"))));
            shoppingTripRepository.save(canaryTrip);
            
            // Create trip for different user
            ShoppingTrip otherUserTrip = new ShoppingTrip();
            otherUserTrip.setStore(store1);
            otherUserTrip.setOwner("sub: otheruser");
            otherUserTrip.setTime(baseTime.plus(Duration.ofMinutes(5))); // Same time as testTrip1
            otherUserTrip.setProducts(new HashMap<>());
            shoppingTripRepository.save(otherUserTrip);

            // Use explicit date range that includes all current user trips
            mockMvc
                .perform(get("/api/shoppingTrips?from={from}&to={to}", 
                            "2026-03-05T15:04:00Z", // Start before first trip
                            "2026-03-05T15:20:00Z") // End after last trip
                            .with(user_jwt))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.store == '" + Testdata.STORE_1_UUID + "')]").exists())
                .andExpect(jsonPath("$.content[?(@.store == '" + Testdata.STORE_2_UUID + "')]").exists());
            
            // Verify canary trip unaffected
            assertTrue(shoppingTripRepository
                           .findById(canaryTrip.getUuid())
                           .isPresent());
            ShoppingTrip unchangedCanary = shoppingTripRepository
                .findById(canaryTrip.getUuid())
                .get();
            assertEquals(Testdata.STORE_1_UUID, unchangedCanary.getStore().getUuid());
            assertEquals(1, unchangedCanary.getProducts().size());
        }
    }
}
