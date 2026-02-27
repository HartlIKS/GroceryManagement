package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
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
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
class ShoppingListControllerTest {
    private MockMvc mockMvc;
    
    @Autowired
    private ShoppingListRepository shoppingListRepository;

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
                    get("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
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
                    get("/api/shoppingLists/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAccessingOtherUsersShoppingList() throws Exception {
            mockMvc
                .perform(
                    get("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenGettingShoppingListWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
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
                    put("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
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
                    put("/api/shoppingLists/{uuid}", Testdata.BAD_UUID)
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
                    put("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
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
                    put("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
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
                    post("/api/shoppingLists")
                        .content(Testdata.SHOPPING_LIST_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/shoppingLists/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.SHOPPING_LIST_3_JSON));
        }

        @Test
        void shouldReturn401WhenCreatingShoppingListWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    post("/api/shoppingLists")
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
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion
            assertFalse(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertEquals(initialCount - 1, shoppingListRepository.count());
            // Verify other list unaffected
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }

        @Test
        void shouldDeleteNonRepeatingShoppingListWhenIfNonRepeatingIsTrue() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion (non-repeating list should be deleted)
            assertFalse(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertEquals(initialCount - 1, shoppingListRepository.count());
            // Verify other list unaffected
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }

        @Test
        void shouldNotDeleteRepeatingShoppingListWhenIfNonRepeatingIsTrue() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user2_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify list was NOT deleted (repeating list should not be deleted when ifNonRepeating=true)
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
            assertEquals(initialCount, shoppingListRepository.count());
            // Verify other list unaffected
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
        }

        @Test
        void shouldDeleteRepeatingShoppingListWhenIfNonRepeatingIsFalse() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .param("ifNonRepeating", "false")
                        .with(user2_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion (repeating list should be deleted when ifNonRepeating=false)
            assertFalse(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
            assertEquals(initialCount - 1, shoppingListRepository.count());
            // Verify other list unaffected
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
        }

        @Test
        void shouldReturn200WhenDeletingNonExistentShoppingList() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify no changes to existing lists
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }

        @Test
        void shouldReturn200WhenDeletingNonExistentShoppingListWithIfNonRepeatingTrue() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.BAD_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify no changes to existing lists
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }

        @Test
        void shouldReturn200WhenDeletingOtherUsersShoppingList() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify no changes (user1 cannot delete user2's list)
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }

        @Test
        void shouldReturn200WhenDeletingOtherUsersShoppingListWithIfNonRepeatingTrue() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_2_UUID)
                        .param("ifNonRepeating", "true")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify no changes (user1 cannot delete user2's list)
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }

        @Test
        void shouldReturn401WhenDeletingShoppingListWithoutAuthentication() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                )
                .andExpect(status().isUnauthorized());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }

        @Test
        void shouldReturn401WhenDeletingShoppingListWithIfNonRepeatingTrueWithoutAuthentication() throws Exception {
            long initialCount = shoppingListRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/shoppingLists/{uuid}", Testdata.SHOPPING_LIST_1_UUID)
                        .param("ifNonRepeating", "true")
                )
                .andExpect(status().isUnauthorized());

            // Verify no changes when unauthorized
            assertEquals(initialCount, shoppingListRepository.count());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_1_UUID, "user1").isPresent());
            assertTrue(shoppingListRepository.findByUuidAndOwner(Testdata.SHOPPING_LIST_2_UUID, "user2").isPresent());
        }
    }

    @Nested
    class SearchShoppingLists {
        @Test
        void shouldReturnUserOwnedShoppingListsWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/api/shoppingLists")
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
                    get("/api/shoppingLists?name=List")
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
                    get("/api/shoppingLists")
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
                    get("/api/shoppingLists")
                )
                .andExpect(status().isUnauthorized());
        }
    }
}
