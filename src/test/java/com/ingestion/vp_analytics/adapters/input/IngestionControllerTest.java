package com.ingestion.vp_analytics.adapters.input;

import com.ingestion.vp_analytics.adapters.input.web.IngestionController;
import com.ingestion.vp_analytics.domain.exception.DuplicateFileException;
import com.ingestion.vp_analytics.domain.exception.EntityNotFoundException;
import com.ingestion.vp_analytics.domain.ports.input.ProcessSpreadsheetInputPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IngestionController.class)
class IngestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProcessSpreadsheetInputPort useCase;

    private static final String CLIENT_ID = "client-id-12321";

    @Test
    void shouldReturn202Success() throws Exception {
        doNothing().when(useCase).execute(any(), anyString());
        MockMultipartFile file = new MockMultipartFile("file", "jan.csv", "text/csv", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/ingestion/spreadsheet").file(file).param("clientId", CLIENT_ID))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturn409Duplicate() throws Exception {
        doThrow(new DuplicateFileException("abc123")).when(useCase).execute(any(), anyString());
        MockMultipartFile file = new MockMultipartFile("file", "jan.csv", "text/csv", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/ingestion/spreadsheet").file(file).param("clientId", CLIENT_ID))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenFileIsEmpty() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("file", "empty.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/v1/ingestion/spreadsheet").file(empty).param("clientId", CLIENT_ID))
                .andExpect(status().isBadRequest());

        verify(useCase, never()).execute(any(), anyString());
    }

    @Test
    void shouldThrowEntityNotFoundException() throws Exception {
        doThrow(new EntityNotFoundException("Upload not found")).when(useCase).execute(any(), anyString());
        MockMultipartFile file = new MockMultipartFile("file", "jan.csv", "text/csv", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/ingestion/spreadsheet").file(file).param("clientId", CLIENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn500UnexpectedFailure() throws Exception {
        doThrow(new RuntimeException("DB down")).when(useCase).execute(any(), anyString());
        MockMultipartFile file = new MockMultipartFile("file", "jan.csv", "text/csv", "data".getBytes());

        mockMvc.perform(multipart("/api/v1/ingestion/spreadsheet").file(file).param("clientId", CLIENT_ID))
                .andExpect(status().isInternalServerError());
    }

}