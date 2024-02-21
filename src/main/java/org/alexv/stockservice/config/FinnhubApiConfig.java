package org.alexv.stockservice.config;

import lombok.RequiredArgsConstructor;
import org.alexv.finnhubclient.client.FinnhubClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@RequiredArgsConstructor
public class FinnhubApiConfig {

    private final Environment environment;

    @Value("${finnhub.api.key}")
    private String apiKey;
    @Bean
    public FinnhubClient finnhubApi() {



        return new FinnhubClient(apiKey);
    }
}
