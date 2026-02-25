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
class ShoppingListControllerTest {
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
    class GetShoppingList {
        @Test
        void shouldReturnShoppingListWhenFoundAndOwned() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_LIST_1_JSON));
        }

        @Test
        void shouldReturn404WhenShoppingListNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAccessingOtherUsersShoppingList() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenGettingShoppingListWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateShoppingList {
        @Test
        void shouldUpdateShoppingListWhenAuthenticatedAndFound() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .content(Testdata.SHOPPING_LIST_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_LIST_1_JSON2));
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentShoppingList() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingLists/{uuid}", Testdata.BAD_UUID)
                        .content(Testdata.SHOPPING_LIST_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenUpdatingOtherUsersShoppingList() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .content(Testdata.SHOPPING_LIST_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenUpdatingShoppingListWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    put("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .content(Testdata.SHOPPING_LIST_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class CreateShoppingList {
        @Test
        void shouldCreateShoppingListWhenAuthenticated() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingLists")
                        .content(Testdata.SHOPPING_LIST_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("/shoppingLists/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_LIST_3_JSON));
        }

        @Test
        void shouldReturn401WhenCreatingShoppingListWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    post("/shoppingLists")
                        .content(Testdata.SHOPPING_LIST_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class DeleteShoppingList {
        @Test
        void shouldDeleteShoppingListWhenAuthenticatedAndOwned() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldDeleteNonRepeatingShoppingListWhenIfNonRepeatingIsTrue() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldNotDeleteRepeatingShoppingListWhenIfNonRepeatingIsTrue() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user2_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldDeleteRepeatingShoppingListWhenIfNonRepeatingIsFalse() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .param("ifNonRepeating", "false")
                        .with(user2_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn200WhenDeletingNonExistentShoppingList() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn200WhenDeletingNonExistentShoppingListWithIfNonRepeatingTrue() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.BAD_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn200WhenDeletingOtherUsersShoppingList() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn200WhenDeletingOtherUsersShoppingListWithIfNonRepeatingTrue() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
        }

        @Test
        void shouldReturn401WhenDeletingShoppingListWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                )
                .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401WhenDeletingShoppingListWithIfNonRepeatingTrueWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    delete("/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .param("ifNonRepeating", "true")
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class SearchShoppingLists {
        @Test
        void shouldReturnUserOwnedShoppingListsWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_LIST_SEARCH_RESULT_JSON));
        }

        @Test
        void shouldReturnUserOwnedShoppingListsWhenSearchingWithName() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists?name=List")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_LIST_SEARCH_RESULT_JSON));
        }

        @Test
        void shouldReturnEmptyResultForUserWithNoLists() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists")
                        .with(user2_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_LIST_SEARCH_RESULT_USER2_JSON));
        }

        @Test
        void shouldReturn401WhenSearchingShoppingListsWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/shoppingLists")
                )
                .andExpect(status().isUnauthorized());
        }
    }
}
