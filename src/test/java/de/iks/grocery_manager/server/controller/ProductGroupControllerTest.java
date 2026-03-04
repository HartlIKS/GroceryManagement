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

import java.util.UUID;

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
    private static final String PRODUCT_GROUP_3_CREATE_JSON = String.format("""
        {
          "name": "Group 3",
          "products": {
            "%s": 1
          }
        }""", Testdata.PRODUCT_3_UUID
    );
    public static final UUID PRODUCT_GROUP_2_UUID = UUID.fromString("20000000-0000-0000-0000-000000000001");
    public static final UUID PRODUCT_GROUP_1_UUID = UUID.fromString("20000000-0000-0000-0000-000000000000");
    private static final String PRODUCT_GROUP_1_UPDATE_JSON = String.format("""
        {
          "name": "Group 1b",
          "products": {
            "%s": 1,
            "%s": 2
          }
        }""", Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID
    );
    private static final String PRODUCT_GROUP_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Group 1",
          "products": {
            "%s": 1
          }
        }""", PRODUCT_GROUP_1_UUID, Testdata.PRODUCT_3_UUID
    );

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
                    get("/api/productGroups/{uuid}", PRODUCT_GROUP_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(PRODUCT_GROUP_1_JSON));
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
                    get("/api/productGroups/{uuid}", PRODUCT_GROUP_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn401WhenGettingProductGroupWithoutAuthentication() throws Exception {
            mockMvc
                .perform(
                    get("/api/productGroups/{uuid}", PRODUCT_GROUP_1_UUID)
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
                    put("/api/productGroups/{uuid}", PRODUCT_GROUP_1_UUID)
                        .content(PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "uuid": "%s",
                      "name": "Group 1b",
                      "products": {
                        "%s": 1,
                        "%s": 2
                      }
                    }""", PRODUCT_GROUP_1_UUID, Testdata.PRODUCT_3_UUID, Testdata.PRODUCT_4_UUID
                )));
            
            // Verify update was applied and other product group unaffected
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentProductGroup() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    put("/api/productGroups/{uuid}", Testdata.BAD_UUID)
                        .content(PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
            
            // Verify no changes to existing product groups
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingOtherUsersProductGroup() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    put("/api/productGroups/{uuid}", PRODUCT_GROUP_2_UUID)
                        .content(PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isNotFound());
            
            // Verify no changes (user1 cannot update user2's group)
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
            assertEquals(initialCount, productGroupRepository.count());
        }

        @Test
        void shouldReturn401WhenUpdatingProductGroupWithoutAuthentication() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    put("/api/productGroups/{uuid}", PRODUCT_GROUP_1_UUID)
                        .content(PRODUCT_GROUP_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
            
            // Verify no changes when unauthorized
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
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
                        .content(PRODUCT_GROUP_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user1_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/productGroups/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "name": "Group 3",
                      "products": {
                        "%s": 1
                      }
                    }""", Testdata.PRODUCT_3_UUID
                )));
            
            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, productGroupRepository.count());
            // Verify existing groups unaffected
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }

        @Test
        void shouldReturn401WhenCreatingProductGroupWithoutAuthentication() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    post("/api/productGroups")
                        .content(PRODUCT_GROUP_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isUnauthorized());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, productGroupRepository.count());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }
    }

    @Nested
    class DeleteProductGroup {
        @Test
        void shouldDeleteProductGroupWhenAuthenticatedAndOwned() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/productGroups/{uuid}", PRODUCT_GROUP_1_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion
            assertFalse(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertEquals(initialCount - 1, productGroupRepository.count());
            // Verify other group unaffected
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
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
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }

        @Test
        void shouldReturn200WhenDeletingOtherUsersProductGroup() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/productGroups/{uuid}", PRODUCT_GROUP_2_UUID)
                        .with(user1_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify no changes (user1 cannot delete user2's group)
            assertEquals(initialCount, productGroupRepository.count());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
        }

        @Test
        void shouldReturn401WhenDeletingProductGroupWithoutAuthentication() throws Exception {
            long initialCount = productGroupRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/productGroups/{uuid}", PRODUCT_GROUP_1_UUID)
                )
                .andExpect(status().isUnauthorized());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, productGroupRepository.count());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_1_UUID, "sub: user1").isPresent());
            assertTrue(productGroupRepository.findByUuidAndOwner(PRODUCT_GROUP_2_UUID, "sub: user2").isPresent());
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
                .andExpect(content().json(
                    String.format(
                        """
                        {
                          "page": {
                            "number": 0,
                            "size": 10,
                            "totalElements": 1,
                            "totalPages": 1
                          },
                          "content": [
                            %s
                          ]
                        }
                        """,
                        PRODUCT_GROUP_1_JSON
                    )
                ));
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
                .andExpect(content().json(
                    String.format(
                        """
                        {
                          "page": {
                            "number": 0,
                            "size": 10,
                            "totalElements": 1,
                            "totalPages": 1
                          },
                          "content": [
                            {
                              "uuid": "%s",
                              "name": "Group 2",
                              "products": {}
                            }
                          ]
                        }
                        """,
                        PRODUCT_GROUP_2_UUID
                    )
                ));
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
