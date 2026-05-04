package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
class ExternalAPIControllerTest {
    private static final String EXTERNAL_API_1_CREATE_JSON = """
        {
          "name": "External API 1"
        }""";
    private static final String EXTERNAL_API_1_UPDATE_JSON = """
        {
          "name": "External API 1 Updated"
        }""";

    private MockMvc mockMvc;

    @Autowired
    private ExternalAPIRepository externalAPIRepository;

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
        
        externalAPIRepository.deleteAll();
    }

    @Nested
    class GetExternalAPI {
        @Test
        void shouldReturnExternalAPIWhenFound() throws Exception {
            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            api = externalAPIRepository.save(api);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}", api.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(api.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("Test API"));
        }

        @Test
        void shouldReturn404WhenExternalAPINotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}", Testdata.BAD_UUID)
                        .with(user_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateExternalAPI {
        @Test
        void shouldUpdateExternalAPIWhenAuthorizedAndFound() throws Exception {
            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            api = externalAPIRepository.save(api);

            long initialCount = externalAPIRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}", api.getUuid())
                        .content(EXTERNAL_API_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.uuid").value(api.getUuid().toString()))
                .andExpect(jsonPath("$.name").value("External API 1 Updated"));

            // Verify update was applied
            assertTrue(externalAPIRepository.findById(api.getUuid()).isPresent());
            assertEquals("External API 1 Updated", externalAPIRepository.findById(api.getUuid()).get().getName());
            assertEquals(initialCount, externalAPIRepository.count());
        }

        @Test
        void shouldReturn404WhenUpdatingNonExistentExternalAPI() throws Exception {
            long initialCount = externalAPIRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}", Testdata.BAD_UUID)
                        .content(EXTERNAL_API_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());

            // Verify no changes
            assertEquals(initialCount, externalAPIRepository.count());
        }

        @Test
        void shouldReturn403WhenUpdatingExternalAPIWithoutAuthorization() throws Exception {
            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            api = externalAPIRepository.save(api);

            long initialCount = externalAPIRepository.count();

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}", api.getUuid())
                        .content(EXTERNAL_API_1_UPDATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, externalAPIRepository.count());
            assertEquals("Test API", externalAPIRepository.findById(api.getUuid()).get().getName());
        }
    }

    @Nested
    class CreateExternalAPI {
        @Test
        void shouldCreateExternalAPIWhenAuthorized() throws Exception {
            long initialCount = externalAPIRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface")
                        .content(EXTERNAL_API_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isCreated())
                .andExpect(header().string("location", matchesRegex("http://localhost/api/masterdata/interface/[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}")))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name").value("External API 1"));

            // Verify creation
            assertEquals(initialCount + 1, externalAPIRepository.count());
        }

        @Test
        void shouldReturn403WhenCreatingExternalAPIWithoutAuthorization() throws Exception {
            long initialCount = externalAPIRepository.count();

            mockMvc
                .perform(
                    post("/api/masterdata/interface")
                        .content(EXTERNAL_API_1_CREATE_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, externalAPIRepository.count());
        }
    }

    @Nested
    class DeleteExternalAPI {
        @Test
        void shouldDeleteExternalAPIWhenAuthorized() throws Exception {
            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            api = externalAPIRepository.save(api);

            long initialCount = externalAPIRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{uuid}", api.getUuid())
                        .with(admin_jwt)
                )
                .andExpect(status().isOk());

            // Verify deletion
            assertFalse(externalAPIRepository.findById(api.getUuid()).isPresent());
            assertEquals(initialCount - 1, externalAPIRepository.count());
        }

        @Test
        void shouldReturn403WhenDeletingExternalAPIWithoutAuthorization() throws Exception {
            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api.setName("Test API");
            api.setProductMappings(new java.util.HashMap<>());
            api.setStoreMappings(new java.util.HashMap<>());
            api = externalAPIRepository.save(api);

            long initialCount = externalAPIRepository.count();

            mockMvc
                .perform(
                    delete("/api/masterdata/interface/{uuid}", api.getUuid())
                        .with(user_jwt)
                )
                .andExpect(status().isForbidden());

            // Verify no changes when unauthorized
            assertEquals(initialCount, externalAPIRepository.count());
            assertTrue(externalAPIRepository.findById(api.getUuid()).isPresent());
        }
    }

    @Nested
    class SearchExternalAPIs {
        @Test
        void shouldReturnAllExternalAPIsWhenSearching() throws Exception {
            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api1 = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api1.setName("API 1");
            api1.setProductMappings(new java.util.HashMap<>());
            api1.setStoreMappings(new java.util.HashMap<>());
            api1 = externalAPIRepository.save(api1);

            de.iks.grocery_manager.server.model.mdi.ExternalAPI api2 = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api2.setName("API 2");
            api2.setProductMappings(new java.util.HashMap<>());
            api2.setStoreMappings(new java.util.HashMap<>());
            api2 = externalAPIRepository.save(api2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'API 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'API 2')]").exists());
        }

        @Test
        void shouldReturnFilteredExternalAPIsWhenSearchingByName() throws Exception {
            // Create test data
            de.iks.grocery_manager.server.model.mdi.ExternalAPI api1 = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api1.setName("Test API 1");
            api1.setProductMappings(new java.util.HashMap<>());
            api1.setStoreMappings(new java.util.HashMap<>());
            api1 = externalAPIRepository.save(api1);

            de.iks.grocery_manager.server.model.mdi.ExternalAPI api2 = new de.iks.grocery_manager.server.model.mdi.ExternalAPI();
            api2.setName("Other API");
            api2.setProductMappings(new java.util.HashMap<>());
            api2.setStoreMappings(new java.util.HashMap<>());
            api2 = externalAPIRepository.save(api2);

            mockMvc
                .perform(
                    get("/api/masterdata/interface")
                        .queryParam("name", "Test")
                        .with(user_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[?(@.name == 'Test API 1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Other API')]").doesNotExist());
        }
    }
}
