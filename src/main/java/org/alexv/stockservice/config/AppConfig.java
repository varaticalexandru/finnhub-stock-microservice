package org.alexv.stockservice.config;

import org.alexv.finnhubclient.client.FinnhubClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public FinnhubClient finnhubClient() {
        String apiKey = System.getenv("apiKey");

        return new FinnhubClient(apiKey);
    }
}
