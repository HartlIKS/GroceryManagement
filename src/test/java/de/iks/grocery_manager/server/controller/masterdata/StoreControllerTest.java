package de.iks.grocery_manager.server.controller.masterdata;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
class StoreControllerTest {
    public static final String STORE_1_JSON = String.format("""
        {
          "uuid": "%s",
          "name": "Store 1",
          "address": {
            "country": "DE",
            "city": "Düsseldorf"
          },
          "currency": "EUR"
        }""", Testdata.STORE_1_UUID);
    public static final String STORE_1_UPDATE_JSON = """
        {
          "name": "Store 1b",
          "address": {
            "city": "Hilden"
          }
        }""";
    public static final String STORE_3_CREATE_JSON = """
        {
          "name": "Store 3",
          "address": {
            "country": "DE",
            "city": "Munich"
          },
          "currency": "EUR"
        }""";
    public static final String STORE_3_JSON = """
        {
          "name": "Store 3",
          "address": {
            "country": "DE",
            "city": "Munich"
          },
          "currency": "EUR"
        }""";
    private MockMvc mockMvc;
    
    @Autowired
    private StoreRepository storeRepository;

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
    class GetStore {
        @Test
        void shouldReturnStoreWhenFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(STORE_1_JSON));
        }

        @Test
        void shouldReturn404WhenStoreNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/store/{uuid}", Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateStore {
        @Test
        void shouldUpdateStoreWhenAuthorizedAndFound() throws Exception {
            long initialCount = storeRepository.count();
            
            mockMvc
                .perform(
                    put("/api/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .content(STORE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "uuid": "%s",
                      "name": "Store 1b",
                      "address": {
                        "country": "DE",
                        "city": "Hilden"
                      },
                      "currency": "EUR"
                    }""", Testdata.STORE_1_UUID)));
            
            // Verify update was applied and other store unaffected
            assertTrue(storeRepository.findById(Testdata.STORE_1_UUID).isPresent());
            assertTrue(storeRepository.findById(Testdata.STORE_2_UUID).isPresent());
            assertEquals(initialCount, storeRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentStore() throws Exception {
            long initialCount = storeRepository.count();
            
            mockMvc
                .perform(
                    put("/api/masterdata/store/{uuid}", Testdata.BAD_UUID)
                        .content(STORE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
            
            // Verify no changes to existing stores
            assertTrue(storeRepository.findById(Testdata.STORE_1_UUID).isPresent());
            assertTrue(storeRepository.findById(Testdata.STORE_2_UUID).isPresent());
            assertEquals(initialCount, storeRepository.count());
        }

        @Test
        void shouldReturn403WhenUpdatingStoreWithoutAuthorization() throws Exception {
            long initialCount = storeRepository.count();
            
            mockMvc
                .perform(
                    put("/api/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .content(STORE_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertTrue(storeRepository.findById(Testdata.STORE_1_UUID).isPresent());
            assertTrue(storeRepository.findById(Testdata.STORE_2_UUID).isPresent());
            assertEquals(initialCount, storeRepository.count());
        }
    }

    @Nested
    class CreateStore {
        @Test
        void shouldCreateStoreWhenAuthorized() throws Exception {
            long initialCount = storeRepository.count();
            
            mockMvc
                .perform(
                    post("/api/masterdata/store")
                        .content(STORE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/store/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(STORE_3_JSON));
            
            // Verify creation - count should increase by 1
            assertEquals(initialCount + 1, storeRepository.count());
            // Verify existing stores unaffected
            assertTrue(storeRepository.findById(Testdata.STORE_1_UUID).isPresent());
            assertTrue(storeRepository.findById(Testdata.STORE_2_UUID).isPresent());
        }

        @Test
        void shouldReturn403WhenCreatingStoreWithoutAuthorization() throws Exception {
            long initialCount = storeRepository.count();
            
            mockMvc
                .perform(
                    post("/api/masterdata/store")
                        .content(STORE_3_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, storeRepository.count());
            assertTrue(storeRepository.findById(Testdata.STORE_1_UUID).isPresent());
            assertTrue(storeRepository.findById(Testdata.STORE_2_UUID).isPresent());
        }
    }

    @Nested
    class DeleteStore {
        @Test
        void shouldDeleteStoreWhenAuthorized() throws Exception {
            long initialCount = storeRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());
            
            // Verify deletion
            assertFalse(storeRepository.findById(Testdata.STORE_1_UUID).isPresent());
            assertEquals(initialCount - 1, storeRepository.count());
            // Verify other store unaffected
            assertTrue(storeRepository.findById(Testdata.STORE_2_UUID).isPresent());
        }

        @Test
        void shouldReturn403WhenDeletingStoreWithoutAuthorization() throws Exception {
            long initialCount = storeRepository.count();
            
            mockMvc
                .perform(
                    delete("/api/masterdata/store/{uuid}", Testdata.STORE_1_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());
            
            // Verify no changes when unauthorized
            assertEquals(initialCount, storeRepository.count());
            assertTrue(storeRepository.findById(Testdata.STORE_1_UUID).isPresent());
            assertTrue(storeRepository.findById(Testdata.STORE_2_UUID).isPresent());
        }
    }

    @Nested
    class SearchStores {
        @Test
        void shouldReturnStoresWhenSearchingByName() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/store")
                        .queryParam("name", "Store")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(String.format("""
                    {
                      "page": {
                        "number": 0,
                        "size": 10,
                        "totalElements": 4,
                        "totalPages": 1
                      },
                      "content": [
                        %s,
                        {
                          "uuid": "%s",
                          "name": "Store 2",
                          "address": {
                            "country": "DE",
                            "city": "Hilden"
                          },
                          "currency": "USD"
                        },
                        %s,
                        {
                          "uuid": "%s",
                          "name": "Store 4",
                          "address": {
                            "country": "DE",
                            "city": "Berlin"
                          },
                          "currency": "USD"
                        }
                      ]
                    }""", STORE_1_JSON, Testdata.STORE_2_UUID, STORE_3_JSON, Testdata.STORE_4_UUID
                )));
        }
    }
}