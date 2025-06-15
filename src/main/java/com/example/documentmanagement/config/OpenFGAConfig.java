package com.example.documentmanagement.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.openfga.sdk.api.client.OpenFgaClient;
import dev.openfga.sdk.api.configuration.ClientConfiguration;

@Configuration
public class OpenFGAConfig {

    @Value("${openfga.api-url}")
    private String apiUrl;

    @Value("${openfga.store-id}")
    private String storeId;

    @Value("${openfga.api-token}")
    private String apiToken;

    @Bean
    public OpenFgaClient openFgaClient() {
        try {
            var configuration = new ClientConfiguration()
                    .apiUrl(apiUrl)
                    .storeId(storeId);

            return new OpenFgaClient(configuration);
        } catch (Exception e) {
            // Log warning but don't fail application startup
            org.slf4j.LoggerFactory.getLogger(OpenFGAConfig.class)
                    .warn("Failed to create OpenFGA client. Application will use fallback authorization. Error: {}", e.getMessage());
            return null;
        }
    }
} 