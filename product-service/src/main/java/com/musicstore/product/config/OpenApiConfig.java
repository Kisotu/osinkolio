package com.musicstore.product.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Product Service API")
                .description("REST API for managing product catalog, categories, and inventory")
                .version("v1.0")
                .contact(new Contact()
                    .name("Music Store Team")
                    .email("dev@musicstore.com"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://musicstore.com/license")));
    }
}