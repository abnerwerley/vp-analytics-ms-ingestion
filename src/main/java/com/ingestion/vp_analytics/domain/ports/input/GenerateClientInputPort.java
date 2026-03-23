package com.ingestion.vp_analytics.domain.ports.input;

import java.util.Map;

public interface GenerateClientInputPort {
    Map<String, String> generate(final String name, final String email);
}
