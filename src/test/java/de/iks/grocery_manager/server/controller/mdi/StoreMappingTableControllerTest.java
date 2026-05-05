package de.iks.grocery_manager.server.controller.mdi;

import de.iks.grocery_manager.server.Testdata;
import de.iks.grocery_manager.server.config.AuthorityConfiguration;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.model.mdi.ExternalAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Sql(Testdata.SCRIPT)
@Transactional
class StoreMappingTableControllerTest {
    private MockMvc mockMvc;

    @Autowired
    private ExternalAPIRepository externalAPIRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private AuthorityConfiguration authorityConfiguration;

    private RequestPostProcessor admin_jwt;

    private ExternalAPI api;

    @BeforeEach
    void setup(WebApplicationContext ctx) {
        admin_jwt = jwt()
            .authorities(new SimpleGrantedAuthority(authorityConfiguration.getMasterdataAuthority()));
        mockMvc = MockMvcBuilders
            .webAppContextSetup(ctx)
            .apply(springSecurity())
            .build();

        // Create ExternalAPI
        api = new ExternalAPI();
        api.setName("Test API");
        api.setProductMappings(new HashMap<>());
        api.setStoreMappings(new HashMap<>());
        api = externalAPIRepository.save(api);
    }

    @Nested
    class TranslateInbound {
        @Test
        void shouldReturnLocalIdWhenMappingExists() throws Exception {
            // Set up mapping
            api.getStoreMappings().put(
                storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow(),
                "remote_store_1"
            );
            api = externalAPIRepository.save(api);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store/in/{remoteId}", api.getUuid(), "remote_store_1")
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(Testdata.STORE_1_UUID.toString()));
        }

        @Test
        void shouldReturn404WhenMappingDoesNotExist() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store/in/{remoteId}", api.getUuid(), "nonexistent_remote_id")
                        .with(admin_jwt)
                )
                .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenApiNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store/in/{remoteId}", Testdata.BAD_UUID, "remote_id")
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SetInboundTranslation {
        @Test
        void shouldSetMappingWhenValid() throws Exception {
            String localIdJson = "\"" + Testdata.STORE_1_UUID + "\"";

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}/mapping/store/in/{remoteId}", api.getUuid(), "remote_store_1")
                        .content(localIdJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(Testdata.STORE_1_UUID.toString()));

            // Verify mapping was set
            api = externalAPIRepository.findById(api.getUuid()).orElseThrow();
            assertEquals("remote_store_1", api.getStoreMappings().get(
                storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow()
            ));
        }

        @Test
        void shouldReturn404WhenLocalStoreNotFound() throws Exception {
            String localIdJson = "\"" + Testdata.BAD_UUID + "\"";

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}/mapping/store/in/{remoteId}", api.getUuid(), "remote_store_1")
                        .content(localIdJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenApiNotFound() throws Exception {
            String localIdJson = "\"" + Testdata.STORE_1_UUID + "\"";

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}/mapping/store/in/{remoteId}", Testdata.BAD_UUID, "remote_store_1")
                        .content(localIdJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class TranslateOutbound {
        @Test
        void shouldReturnRemoteIdWhenMappingExists() throws Exception {
            // Set up mapping
            api.getStoreMappings().put(
                storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow(),
                "remote_store_1"
            );
            api = externalAPIRepository.save(api);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store/out/{localId}", api.getUuid(), Testdata.STORE_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("remote_store_1"));
        }

        @Test
        void shouldReturn404WhenMappingDoesNotExist() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store/out/{localId}", api.getUuid(), Testdata.STORE_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturn404WhenApiNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store/out/{localId}", Testdata.BAD_UUID, Testdata.STORE_1_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class SetOutboundTranslation {
        @Test
        void shouldSetMappingWhenValid() throws Exception {
            String remoteIdJson = "\"remote_store_1\"";

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}/mapping/store/out/{localId}", api.getUuid(), Testdata.STORE_1_UUID)
                        .content(remoteIdJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value("remote_store_1"));

            // Verify mapping was set
            api = externalAPIRepository.findById(api.getUuid()).orElseThrow();
            assertEquals("remote_store_1", api.getStoreMappings().get(
                storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow()
            ));
        }

        @Test
        void shouldReturn404WhenLocalStoreNotFound() throws Exception {
            String remoteIdJson = "\"remote_store_1\"";

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}/mapping/store/out/{localId}", api.getUuid(), Testdata.BAD_UUID)
                        .content(remoteIdJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturn404WhenApiNotFound() throws Exception {
            String remoteIdJson = "\"remote_store_1\"";

            mockMvc
                .perform(
                    put("/api/masterdata/interface/{uuid}/mapping/store/out/{localId}", Testdata.BAD_UUID, Testdata.STORE_1_UUID)
                        .content(remoteIdJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetMappings {
        @Test
        void shouldReturnMappingsWhenMappingsExist() throws Exception {
            // Set up mappings
            api.getStoreMappings().put(
                storeRepository.findById(Testdata.STORE_1_UUID).orElseThrow(),
                "remote_store_1"
            );
            api.getStoreMappings().put(
                storeRepository.findById(Testdata.STORE_2_UUID).orElseThrow(),
                "remote_store_2"
            );
            api = externalAPIRepository.save(api);

            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store", api.getUuid())
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$." + Testdata.STORE_1_UUID).value("remote_store_1"))
                .andExpect(jsonPath("$." + Testdata.STORE_2_UUID).value("remote_store_2"));
        }

        @Test
        void shouldReturnEmptyMapWhenNoMappingsExist() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store", api.getUuid())
                        .with(admin_jwt)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        void shouldReturn404WhenApiNotFound() throws Exception {
            mockMvc
                .perform(
                    get("/api/masterdata/interface/{uuid}/mapping/store", Testdata.BAD_UUID)
                        .with(admin_jwt)
                )
                .andExpect(status().isNotFound());
        }
    }
}
