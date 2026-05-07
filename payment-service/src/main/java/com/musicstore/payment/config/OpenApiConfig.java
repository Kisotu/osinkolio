package com.musicstore.payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment Service API")
                        .version("1.0.0")
                        .description("RESTful API for payment processing and management")
                        .contact(new Contact()
                                .name("MusicStore Support")
                                .email("support@musicstore.com"))
                        .license(new License()
                                .name("MIT License")  
                                .url("https://musicstore.com/licenses/mit")));
    }
}
