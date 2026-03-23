package com.ingestion.vp_analytics.domain.ports.input;

import org.springframework.web.multipart.MultipartFile;

public interface ProcessSpreadsheetInputPort {
    void execute(MultipartFile file, String clientId);
}
