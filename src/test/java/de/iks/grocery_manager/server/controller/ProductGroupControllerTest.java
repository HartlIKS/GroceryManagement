package de.iks.grocery_manager.server.controller;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
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
class ProductGroupControllerTest {
    private MockMvc mockMvc;
    
    @Autowired
    private ProductGroupRepository productGroupRepository;

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
    class GetProductGroup {
        @Test
        void shouldReturnProductGroupWhenFoundAndOwned() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_GROUP_1_JSON));
        }

        @Test
        void shouldReturn404WhenProductGroupNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenAccessingOtherUsersProductGroup() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenGettingProductGroupWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_1_UUID)
                )
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UpdateProductGroup {
        @Test
        void shouldUpdateProductGroupWhenAuthenticatedAndFound() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    put("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_1_UUID)
                        .content(Testdata.PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_GROUP_1_JSON2));
            
            // Verify update was applied and other product group unaffected
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentProductGroup() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    put("/api/productGroups/{uuid}", Testdata.BAD_UUID)
                        .content(Testdata.PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
            
            // Verify no changes to existing product groups
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingOtherUsersProductGroup() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    put("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_2_UUID)
                        .content(Testdata.PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
            
            // Verify no changes (user1 cannot update user2's group)
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn401WhenUpdatingProductGroupWithoutAuthentication() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    put("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_1_UUID)
                        .content(Testdata.PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
            
            // Verify no changes when unauthorized
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }
    }

    @Nested
    class CreateProductGroup {
        @Test
        void shouldCreateProductGroupWhenAuthenticated() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    post("/api/productGroups")
                        .content(Testdata.PRODUCT_GROUP_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/productGroups/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_GROUP_3_JSON));
            
            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, productGroupRepository.count());
            // Verify existing groups unaffected
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }

        @Test
        void shouldReturn401WhenCreatingProductGroupWithoutAuthentication() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    post("/api/productGroups")
                        .content(Testdata.PRODUCT_GROUP_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, productGroupRepository.count());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }
    }

    @Nested
    class DeleteProductGroup {
        @Test
        void shouldDeleteProductGroupWhenAuthenticatedAndOwned() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion
            assertFalse(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertEquals(initialCount - 1, productGroupRepository.count());
            // Verify other group unaffected
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }

        @Test
        void shouldReturn200WhenDeletingNonExistentProductGroup() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/productGroups/{uuid}", Testdata.BAD_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify no changes to existing groups
            assertEquals(initialCount, productGroupRepository.count());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }

        @Test
        void shouldReturn200WhenDeletingOtherUsersProductGroup() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify no changes (user1 cannot delete user2's group)
            assertEquals(initialCount, productGroupRepository.count());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }

        @Test
        void shouldReturn401WhenDeletingProductGroupWithoutAuthentication() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/productGroups/{uuid}", Testdata.PRODUCT_GROUP_1_UUID)
                )
                .andExpect(status().isUnauthorized());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, productGroupRepository.count());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(Testdata.PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }
    }

    @Nested
    class SearchProductGroups {
        @Test
        void shouldReturnUserOwnedProductGroupsWhenSearching() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups")
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_GROUP_SEARCH_RESULT_JSON));
        }

        @Test
        void shouldReturnEmptyResultForUserWithNoGroups() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups")
                        .with(user2_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(Testdata.PRODUCT_GROUP_SEARCH_RESULT_USER2_JSON));
        }

        @Test
        void shouldReturn401WhenSearchingProductGroupsWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups")
                )
                .andExpect(status().isUnauthorized());
        }
    }


}
