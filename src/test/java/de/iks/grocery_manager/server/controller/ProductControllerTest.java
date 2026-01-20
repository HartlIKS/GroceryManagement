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
class ProductControllerTest {
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
    class GetProduct {
        @Test
        void shouldReturnProductWhenFound() throws Exception {
            mockMvc
                .perform(
                    get("/product/{uuid}", Testdata.PRODUCT_1_UUID)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_1_JSON));
        }

        @Test
        void shouldReturn404WhenProductNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/product/{uuid}", Testdata.BAD_UUID)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateProduct {
        @Test
        void shouldUpdateProductWhenAuthorizedAndFound() throws Exception {
            mockMvc
                .perform(
                    put("/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .content(Testdata.PRODUCT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_1_JSON2));
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentProduct() throws Exception {
            mockMvc
                .perform(
                    put("/product/{uuid}", Testdata.BAD_UUID)
                        .content(Testdata.PRODUCT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403WhenUpdatingProductWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    put("/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .content(Testdata.PRODUCT_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class CreateProduct {
        @Test
        void shouldCreateProductWhenAuthorized() throws Exception {
            mockMvc
                .perform(
                    post("/product")
                        .content(Testdata.PRODUCT_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("/product/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_3_JSON));
        }

        @Test
        void shouldReturn403WhenCreatingProductWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    post("/product")
                        .content(Testdata.PRODUCT_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteProduct {
        @Test
        void shouldDeleteProductWhenAuthorized() throws Exception {
            mockMvc
                .perform(
                    delete("/product/{uuid}", Testdata.PRODUCT_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn403WhenDeletingProductWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    delete("/product/{uuid}", Testdata.PRODUCT_1_UUID)
                )
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class SearchProducts {
        @Test
        void shouldReturnAllProductsWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/product")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_SEARCH_RESULT_JSON));
        }
    }
}