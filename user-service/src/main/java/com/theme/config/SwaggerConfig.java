package com.theme.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${gateway.service-url}")
    private String serviceUrl;

    @Bean
    public OpenAPI openAPI() {
        Server server = new Server();
        server.setUrl(serviceUrl);
        return new OpenAPI()
                .servers(List.of(server))
                .info(getInfo());
    }

    public Info getInfo() {
        return new Info()
                .title("User Service API Documentation")
                .version("1.0");
    }
}
