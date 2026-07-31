package com.minidesk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miniDeskOpenAPI() {

        return new OpenAPI()

                .info(
                        new Info()
                                .title("MiniDesk API")
                                .description("Mini système de gestion de tickets avec IA")
                                .version("1.0")

                                .contact(
                                        new Contact()
                                                .name("Khmaich")
                                                .email("abdellahkhmich.12@gmail.com")
                                )
                );

    }

}