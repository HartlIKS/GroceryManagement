package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static de.iks.grocery_manager.server.config.SecurityConfiguration.AUTHORITY_MASTERDATA;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
class PriceListControllerTest {
    private MockMvc mockMvc;

    private final RequestPostProcessor admin_jwt = jwt()
        .authorities(new SimpleGrantedAuthority(AUTHORITY_MASTERDATA));

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
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdatePrice {
        @Test
        void shouldUpdatePriceWhenAuthorizedAndFound() throws Exception {
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
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentPrice() throws Exception {
            mockMvc
                .perform(
                    put("/masterdata/price/{uuid}", Testdata.BAD_UUID)
                        .content(Testdata.PRICE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenUpdatingPriceWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    put("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .content(Testdata.PRICE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreatePrice {
        @Test
        void shouldCreatePriceWhenAuthorized() throws Exception {
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
        }

        @Test
        void shouldReturn401WhenCreatingPriceWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    post("/masterdata/price")
                        .content(Testdata.PRICE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeletePrice {
        @Test
        void shouldDeletePriceWhenAuthorized() throws Exception {
            mockMvc
                .perform(
                    delete("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn401WhenDeletingPriceWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    delete("/masterdata/price/{uuid}", Testdata.PRICE_1_UUID)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class SearchPrices {
        @Test
        void shouldReturnAllPricesWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/price")
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
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].store").value(Testdata.STORE_3_UUID.toString()))
                .andExpect(jsonPath("$.content[0].product").value(Testdata.PRODUCT_GROUP_TEST_1_UUID.toString()));
        }
    }
}
