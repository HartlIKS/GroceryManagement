package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.masterdata.PriceRepository;
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

import java.time.ZonedDateTime;

import static de.iks.grocery_manager.server.config.SecurityConfiguration.AUTHORITY_MASTERDATA;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
class PriceListControllerTest {
    private MockMvc mockMvc;
    
    @Autowired
    private PriceRepository priceRepository;

    private final RequestPostProcessor admin_jwt = jwt()
        .authorities(new SimpleGrantedAuthority(AUTHORITY_MASTERDATA));
    private final RequestPostProcessor user_jwt = jwt();

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();
    }

    @Nested
    class GetPrice {
        @Test
        void shouldReturnPriceWhenFound() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRICE_1_JSON));
        }

        @Test
        void shouldReturn404WhenPriceNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/price/{uuid}", Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdatePrice {
        @Test
        void shouldUpdatePriceWhenAuthorizedAndFound() throws Exception {
            long initialCount = priceRepository.count();
            
            mockMvc
                .perform(
                    put("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .content(Testdata.PRICE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRICE_1_JSON2));
            
            // Verify update was applied and other price unaffected
            assertTrue(priceRepository.findById(Testdata.PRICE_1_UUID).isPresent());
            assertTrue(priceRepository.findById(Testdata.PRICE_2_UUID).isPresent());
            assertEquals(initialCount, priceRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentPrice() throws Exception {
            long initialCount = priceRepository.count();
            
            mockMvc
                .perform(
                    put("/masterdata/price/{uuid}", Testdata.BAD_UUID)
                        .content(Testdata.PRICE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
            
            // Verify no changes to existing prices
            assertTrue(priceRepository.findById(Testdata.PRICE_1_UUID).isPresent());
            assertTrue(priceRepository.findById(Testdata.PRICE_2_UUID).isPresent());
            assertEquals(initialCount, priceRepository.count());
        }

        @Test
        void shouldReturn403WhenUpdatingPriceWithoutAuthorization() throws Exception {
            long initialCount = priceRepository.count();
            
            mockMvc
                .perform(
                    put("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .content(Testdata.PRICE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertTrue(priceRepository.findById(Testdata.PRICE_1_UUID).isPresent());
            assertTrue(priceRepository.findById(Testdata.PRICE_2_UUID).isPresent());
            assertEquals(initialCount, priceRepository.count());
        }
    }

    @Nested
    class CreatePrice {
        @Test
        void shouldCreatePriceWhenAuthorized() throws Exception {
            long initialCount = priceRepository.count();
            
            mockMvc
                .perform(
                    post("/masterdata/price")
                        .content(Testdata.PRICE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("/price/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRICE_3_JSON));
            
            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, priceRepository.count());
            // Verify existing prices unaffected
            assertTrue(priceRepository.findById(Testdata.PRICE_1_UUID).isPresent());
            assertTrue(priceRepository.findById(Testdata.PRICE_2_UUID).isPresent());
        }

        @Test
        void shouldReturn403WhenCreatingPriceWithoutAuthorization() throws Exception {
            long initialCount = priceRepository.count();
            
            mockMvc
                .perform(
                    post("/masterdata/price")
                        .content(Testdata.PRICE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, priceRepository.count());
            assertTrue(priceRepository.findById(Testdata.PRICE_1_UUID).isPresent());
            assertTrue(priceRepository.findById(Testdata.PRICE_2_UUID).isPresent());
        }
    }

    @Nested
    class DeletePrice {
        @Test
        void shouldDeletePriceWhenAuthorized() throws Exception {
            long initialCount = priceRepository.count();
            
            mockMvc
                .perform(
                    delete("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion
            assertFalse(priceRepository.findById(Testdata.PRICE_1_UUID).isPresent());
            assertEquals(initialCount - 1, priceRepository.count());
            // Verify other price unaffected
            assertTrue(priceRepository.findById(Testdata.PRICE_2_UUID).isPresent());
        }

        @Test
        void shouldReturn403WhenDeletingPriceWithoutAuthorization() throws Exception {
            long initialCount = priceRepository.count();
            
            mockMvc
                .perform(
                    delete("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, priceRepository.count());
            assertTrue(priceRepository.findById(Testdata.PRICE_1_UUID).isPresent());
            assertTrue(priceRepository.findById(Testdata.PRICE_2_UUID).isPresent());
        }
    }

    @Nested
    class SearchPrices {
        @Test
        void shouldReturnAllPricesWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRICE_SEARCH_RESULT_JSON));
        }

        @Test
        void shouldReturnPricesWhenSearchingByStore() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("store", Testdata.STORE_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].store").value(Testdata.STORE_3_UUID.toString()));
        }

        @Test
        void shouldReturnPricesWhenSearchingByProduct() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("product", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].product").value(Testdata.PRODUCT_GROUP_TEST_1_UUID.toString()));
        }

        @Test
        void shouldReturnPricesWhenSearchingByStoreAndProduct() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("store", Testdata.STORE_3_UUID.toString())
                        .queryParam("product", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].store").value(Testdata.STORE_3_UUID.toString()))
                .andExpect(jsonPath("$.content[0].product").value(Testdata.PRODUCT_GROUP_TEST_1_UUID.toString()));
        }
    }

    @Nested
    class SearchPricesWithDateStoresAndProducts {
        @Test
        void shouldReturnPricesWhenSearchingWithValidDateStoresAndProducts() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID).isArray())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID + "[0].listPriceUUID").value(Testdata.PRICE_1_UUID.toString()))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID + "[0].price").value(11));
        }

        @Test
        void shouldReturnMultiplePricesWhenSearchingWithMultipleStoresAndProducts() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID + "," + Testdata.STORE_4_UUID)
                        .queryParam("products", Testdata.PRODUCT_GROUP_TEST_1_UUID + "," +
                            Testdata.PRODUCT_GROUP_TEST_2_UUID
                        )
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_2_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_2_UUID + "." + Testdata.STORE_4_UUID).exists())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1))
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_GROUP_TEST_2_UUID + "." + Testdata.STORE_4_UUID + ".size()").value(1));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithDateOutsideValidRange() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2023-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(0));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithNonExistentStore() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.BAD_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(0));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithNonExistentProduct() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.BAD_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(0));
        }

        @Test
        void shouldReturnPricesWhenSearchingWithDateAtValidFromBoundary() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2024-01-01T00:00:00Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1));
        }

        @Test
        void shouldReturnPricesWhenSearchingWithDateAtValidToBoundary() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2024-12-31T23:59:59Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_GROUP_TEST_1_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithDateJustAfterValidTo() throws Exception {
            ZonedDateTime searchDate = ZonedDateTime.parse("2025-01-01T00:00:00Z");
            
            mockMvc
                .perform(
                    get("/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_GROUP_TEST_1_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(0));
        }
    }
}
