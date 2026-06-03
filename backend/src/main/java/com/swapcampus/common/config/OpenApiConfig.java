package com.swapcampus.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI swapCampusOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SwapCampus API")
                        .version("0.1.0")
                        .description("REST and WebSocket API scaffold for SwapCampus."));
    }
}
