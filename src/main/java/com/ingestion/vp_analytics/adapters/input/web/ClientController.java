package com.ingestion.vp_analytics.adapters.input.web;

import com.ingestion.vp_analytics.domain.ports.input.GenerateClientInputPort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/client")
public class ClientController {

    private final GenerateClientInputPort useCase;

    public ClientController(final GenerateClientInputPort useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@RequestBody final ClientRequest request) {
        return useCase.generate(request.name(), request.email());
    }
}
