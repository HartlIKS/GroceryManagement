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
class StoreControllerTest {
    private MockMvc mockMvc;

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
    class GetStore {
        @Test
        void shouldReturnStoreWhenFound() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.STORE_1_JSON));
        }

        @Test
        void shouldReturn404WhenStoreNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/store/{uuid}", Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateStore {
        @Test
        void shouldUpdateStoreWhenAuthorizedAndFound() throws Exception {
            mockMvc
                .perform(
                    put("/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .content(Testdata.STORE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.STORE_1_JSON2));
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentStore() throws Exception {
            mockMvc
                .perform(
                    put("/masterdata/store/{uuid}", Testdata.BAD_UUID)
                        .content(Testdata.STORE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn403WhenUpdatingStoreWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    put("/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .content(Testdata.STORE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class CreateStore {
        @Test
        void shouldCreateStoreWhenAuthorized() throws Exception {
            mockMvc
                .perform(
                    post("/masterdata/store")
                        .content(Testdata.STORE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("/store/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.STORE_3_JSON));
        }

        @Test
        void shouldReturn403WhenCreatingStoreWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    post("/masterdata/store")
                        .content(Testdata.STORE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class DeleteStore {
        @Test
        void shouldDeleteStoreWhenAuthorized() throws Exception {
            mockMvc
                .perform(
                    delete("/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn403WhenDeletingStoreWithoutAuthorization() throws Exception {
            mockMvc
                .perform(
                    delete("/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    class SearchStores {
        @Test
        void shouldReturnStoresWhenSearchingByName() throws Exception {
            mockMvc
                .perform(
                    get("/masterdata/store")
                        .queryParam("name", "Store")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.STORE_SEARCH_RESULT_JSON));
        }
    }
}