package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
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

import java.time.Instant;

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
    private static final String PRICE_3_JSON = String.format("""
        {
          "store": "%s",
          "product": "%s",
          "validFrom": "2024-03-01T00:00:00Z",
          "validTo": "2024-12-31T23:59:59Z",
          "price": 8.99
        }""", Testdata.STORE_1_UUID, Testdata.PRODUCT_4_UUID
    );
    private static final String PRICE_3_CREATE_JSON = PRICE_3_JSON;
    private static final String PRICE_1_UPDATE_JSON = """
        {
          "validFrom": "2024-02-01T00:00:00Z",
          "validTo": "2024-11-30T23:59:59Z",
          "price": 12.99
        }""";
    private static final String PRICE_1_JSON = String.format("""
        {
          "uuid": "%s",
          "store": "%s",
          "product": "%s",
          "validFrom": "2024-01-01T00:00:00Z",
          "validTo": "2024-12-31T23:59:59Z",
          "price": 11
        }""", Testdata.PRICE_1_UUID, Testdata.STORE_3_UUID, Testdata.PRODUCT_3_UUID
    );

    private MockMvc mockMvc;
    
    @Autowired
    private PriceRepository priceRepository;

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
    class GetPrice {
        @Test
        void shouldReturnPriceWhenFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PRICE_1_JSON));
        }

        @Test
        void shouldReturn404WhenPriceNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/price/{uuid}", Testdata.BAD_UUID)
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
                    put("/api/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .content(PRICE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "uuid": "%s",
                      "store": "%s",
                      "product": "%s",
                      "validFrom": "2024-02-01T00:00:00Z",
                      "validTo": "2024-11-30T23:59:59Z",
                      "price": 12.99
                    }""", Testdata.PRICE_1_UUID, Testdata.STORE_3_UUID, Testdata.PRODUCT_3_UUID
                )));
            
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
                    put("/api/masterdata/price/{uuid}", Testdata.BAD_UUID)
                        .content(PRICE_1_UPDATE_JSON)
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
                    put("/api/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .content(PRICE_1_UPDATE_JSON)
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
                    post("/api/masterdata/price")
                        .content(PRICE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/masterdata/price/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PRICE_3_JSON));
            
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
                    post("/api/masterdata/price")
                        .content(PRICE_3_CREATE_JSON)
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
                    delete("/api/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
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
                    delete("/api/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
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
                    get("/api/masterdata/price")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "page": {
                        "number": 0,
                        "size": 10,
                        "totalElements": 2,
                        "totalPages": 1
                      },
                      "content": [
                        %s,
                        {
                          "uuid": "%s",
                          "store": "%s",
                          "product": "%s",
                          "validFrom": "2024-01-01T00:00:00Z",
                          "validTo": "2024-12-31T23:59:59Z",
                          "price": 5
                        }
                      ]
                    }""", PRICE_1_JSON, Testdata.PRICE_2_UUID, Testdata.STORE_4_UUID, Testdata.PRODUCT_4_UUID
                )));
        }

        @Test
        void shouldReturnPricesWhenSearchingByStore() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/price")
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
                    get("/api/masterdata/price")
                        .queryParam("product", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].product").value(Testdata.PRODUCT_3_UUID.toString()));
        }

        @Test
        void shouldReturnPricesWhenSearchingByStoreAndProduct() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("store", Testdata.STORE_3_UUID.toString())
                        .queryParam("product", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].store").value(Testdata.STORE_3_UUID.toString()))
                .andExpect(jsonPath("$.content[0].product").value(Testdata.PRODUCT_3_UUID.toString()));
        }
    }

    @Nested
    class SearchPricesWithDateStoresAndProducts {
        @Test
        void shouldReturnPricesWhenSearchingWithValidDateStoresAndProducts() throws Exception {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID).isArray())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID + "[0].listPriceUUID").value(Testdata.PRICE_1_UUID.toString()))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID + "[0].price").value(11));
        }

        @Test
        void shouldReturnMultiplePricesWhenSearchingWithMultipleStoresAndProducts() throws Exception {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID + "," + Testdata.STORE_4_UUID)
                        .queryParam("products", Testdata.PRODUCT_3_UUID + "," +
                            Testdata.PRODUCT_4_UUID
                        )
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_4_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." + Testdata.PRODUCT_4_UUID + "." + Testdata.STORE_4_UUID).exists())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1))
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_4_UUID + "." + Testdata.STORE_4_UUID + ".size()").value(1));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithDateOutsideValidRange() throws Exception {
            Instant searchDate = Instant.parse("2023-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(0));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithNonExistentStore() throws Exception {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.BAD_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(0));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithNonExistentProduct() throws Exception {
            Instant searchDate = Instant.parse("2024-06-15T10:00:00Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
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
            Instant searchDate = Instant.parse("2024-01-01T00:00:00Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1));
        }

        @Test
        void shouldReturnPricesWhenSearchingWithDateAtValidToBoundary() throws Exception {
            Instant searchDate = Instant.parse("2024-12-31T23:59:59Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$." + Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID).exists())
                .andExpect(jsonPath("$." +
                                        Testdata.PRODUCT_3_UUID + "." + Testdata.STORE_3_UUID + ".size()").value(1));
        }

        @Test
        void shouldReturnEmptyMapWhenSearchingWithDateJustAfterValidTo() throws Exception {
            Instant searchDate = Instant.parse("2025-01-01T00:00:00Z");
            
            mockMvc
                .perform(
                    get("/api/masterdata/price")
                        .queryParam("at", searchDate.toString())
                        .queryParam("stores", Testdata.STORE_3_UUID.toString())
                        .queryParam("products", Testdata.PRODUCT_3_UUID.toString())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.size()").value(0));
        }
    }
}
