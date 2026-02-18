package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
class ShoppingTripControllerTest {
    private MockMvc mockMvc;

    private final RequestPostProcessor user1_jwt = jwt()
        .jwt(b -> b.subject("user1"));
    
    private final RequestPostProcessor user2_jwt = jwt()
        .jwt(b -> b.subject("user2"));

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();
    }

    @Nested
    class GetShoppingTrip {
        @Test
        void shouldReturnShoppingTripWhenFoundAndOwned() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_TRIP_1_JSON));
        }

        @Test
        void shouldReturn404WhenShoppingTripNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAccessingOtherUsersShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenGettingShoppingTripWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_1_UUID)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateShoppingTrip {
        @Test
        void shouldUpdateShoppingTripWhenAuthenticatedAndFound() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_TRIP_1_JSON2));
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingTrips/{uuid}", Testdata.BAD_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenUpdatingOtherUsersShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_2_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenUpdatingShoppingTripWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateShoppingTrip {
        @Test
        void shouldCreateShoppingTripWhenAuthenticated() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips")
                        .content(Testdata.SHOPPING_TRIP_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("/shoppingLists/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_TRIP_3_JSON));
        }

        @Test
        void shouldReturn401WhenCreatingShoppingTripWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips")
                        .content(Testdata.SHOPPING_TRIP_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteShoppingTrip {
        @Test
        void shouldDeleteShoppingTripWhenAuthenticatedAndOwned() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn200WhenDeletingNonExistentShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingTrips/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn200WhenDeletingOtherUsersShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn401WhenDeletingShoppingTripWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingTrips/{uuid}", Testdata.SHOPPING_TRIP_1_UUID)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class AddToShoppingTrip {
        @Test
        void shouldAddProductsToShoppingTripWhenAuthenticatedAndOwned() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_MULTIPLE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(Testdata.SHOPPING_TRIP_1_UUID.toString()))
                .andExpect(jsonPath("$.store").value(Testdata.STORE_3_UUID.toString()))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_1_UUID + "']").value(4)) // 2 (existing) + 2 (added)
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_2_UUID + "']").value(3)); // 0 (existing) + 3 (added)
        }

        @Test
        void shouldAddSingleProductToShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_SINGLE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(Testdata.SHOPPING_TRIP_1_UUID.toString()))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_1_UUID + "']").value(2)) // unchanged
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_2_UUID + "']").value(1.5)); // newly added
        }

        @Test
        void shouldAccumulateAmountsWhenAddingExistingProduct() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_EXISTING_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_1_UUID + "']").value(3)); // 2 (existing) + 1 (added)
        }

        @Test
        void shouldReturn404WhenAddingToNonExistentShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.BAD_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_EXISTING_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAddingToOtherUsersShoppingTrip() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_2_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_EXISTING_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenAddingToShoppingTripWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_EXISTING_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldHandleEmptyProductsMap() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_EMPTY_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_1_UUID + "']").value(2)); // unchanged
        }

        @Test
        void shouldHandleZeroAmount() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_ZERO_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_1_UUID + "']").value(2)); // unchanged
        }

        @Test
        void shouldHandleNegativeAmount() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingTrips/{uuid}/add", Testdata.SHOPPING_TRIP_1_UUID)
                        .content(Testdata.SHOPPING_TRIP_1_ADD_NEGATIVE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products").isMap())
                .andExpect(jsonPath("$.products['" + Testdata.PRODUCT_GROUP_TEST_1_UUID + "']").value(2)); // unchanged
        }
    }

    @Nested
    class SearchShoppingTrips {
        @Test
        void shouldReturnUserOwnedShoppingTripsWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips?from=2024-01-01T00:00:00Z&to=2024-01-31T23:59:59Z")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_TRIP_SEARCH_RESULT_JSON));
        }

        @Test
        void shouldReturnUserOwnedShoppingTripsWhenSearchingWithFrom() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips?from=2024-01-01T00:00:00Z&to=2024-01-31T23:59:59Z")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_TRIP_SEARCH_RESULT_JSON));
        }

        @Test
        void shouldReturnUserOwnedShoppingTripsWhenSearchingWithTo() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips?from=2024-01-01T00:00:00Z&to=2024-01-31T23:59:59Z")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_TRIP_SEARCH_RESULT_JSON));
        }

        @Test
        void shouldReturnEmptyResultForUserWithNoTrips() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips?from=2024-01-01T00:00:00Z&to=2024-01-31T23:59:59Z")
                        .with(user2_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_TRIP_SEARCH_RESULT_USER2_JSON));
        }

        @Test
        void shouldReturn401WhenSearchingShoppingTripsWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingTrips")
                )
                .andExpect(status().isUnauthorized());
        }
    }
}
