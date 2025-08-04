package com.komentum.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    Server server = new Server();
    return new OpenAPI()
        .servers(List.of(server))
        .info(info());
  }

  @Bean
  public Info info() {
    return new Info()
        .title("Komentum Service")
        .description("Komentum service api test")
        .version("1.0.0");
  }
}
