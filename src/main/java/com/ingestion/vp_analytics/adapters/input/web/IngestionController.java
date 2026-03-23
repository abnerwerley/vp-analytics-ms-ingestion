package com.ingestion.vp_analytics.adapters.input.web;

import com.ingestion.vp_analytics.domain.exception.EmptyFileException;
import com.ingestion.vp_analytics.domain.ports.input.ProcessSpreadsheetInputPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ingestion")
public class IngestionController {

    private final ProcessSpreadsheetInputPort useCase;

    public IngestionController(final ProcessSpreadsheetInputPort useCase) {
        this.useCase = useCase;
    }

    @PostMapping("/spreadsheet")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingest(@RequestParam("file") final MultipartFile file,
                       @RequestParam("clientId") final String clientId) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        useCase.execute(file, clientId);
    }
}
