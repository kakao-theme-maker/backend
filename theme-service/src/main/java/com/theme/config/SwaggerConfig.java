package com.theme.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        String serviceUrl = "http://localhost:8081";
        server.setUrl(serviceUrl);
        return new OpenAPI()
                .servers(List.of(server))
                .info(info());
    }

    @Bean
    public Info info() {
        return new Info()
                .title("Theme Service")
                .description("theme service api test")
                .version("1.0.0");
    }
}
