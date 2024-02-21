package org.alexv.stockservice.config;

import org.alexv.finnhubclient.client.FinnhubClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FinnhubApiConfig {
    @Value("${finnhub.api.key}")
    private String apiKey;
    @Bean
    public FinnhubClient finnhubApi() {

        return new FinnhubClient(apiKey);
    }
}
