package com.ingestion.vp_analytics.adapters.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ingestion.vp_analytics.adapters.input.web.ClientController;
import com.ingestion.vp_analytics.adapters.input.web.ClientRequest;
import com.ingestion.vp_analytics.domain.exception.ClientAlreadyExistsException;
import com.ingestion.vp_analytics.domain.usecase.GenerateClientUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenerateClientUseCase useCase;

    public static final String CLIENT_ID = "id-gerado-teste";
    public static final String NAME = "Marcenaria legal";
    public static final String EMAIL = "marcenarialegal@gmail.com";

    @Test
    void shouldCreateUser() throws Exception {
        when(useCase.generate(NAME, EMAIL))
                .thenReturn(Map.of("clientId", CLIENT_ID)
                );

        ClientRequest request = new ClientRequest(NAME, EMAIL);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID));
    }

    @Test
    void shouldThrowConflict() throws Exception {
        when(useCase.generate(NAME, EMAIL))
                .thenThrow(new ClientAlreadyExistsException());

        ClientRequest request = new ClientRequest(NAME, EMAIL);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/client")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
