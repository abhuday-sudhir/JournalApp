package com.springboot.JournalApp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI myCustomConfig()
    {
        return new OpenAPI().info(
                new Info().title("My Journal App")
                        .description("By Abhuday"))
                .servers(Arrays.asList(
                    new Server().url("http://localhost:8081").description("dev"),
                    new Server().url("hhtp://localhost:8082").description("production")));
    }
}
