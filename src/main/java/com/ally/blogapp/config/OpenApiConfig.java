package com.ally.blogapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI blogAppOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blog Platform API")
                        .description("RESTful and GraphQL API for the Blogging Platform. "
                                + "Repository-driven endpoints with pagination, sorting, "
                                + "Caffeine-backed caching, and tuned transactional boundaries.")
                        .version("1.1.0"));
    }
}
