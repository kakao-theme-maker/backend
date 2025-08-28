package com.komentum.config;

import com.komentum.auth.AuthProperty;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    Server server = new Server();
    return new OpenAPI()
        .servers(List.of(server))
        .addSecurityItem(getTokenSecurityRequirement())
        .components(getTokenComponents())
        .info(info());
  }

  public Info info() {
    return new Info()
        .title("Komentum Service")
        .description("Komentum service api test")
        .version("1.0.0");
  }

  public Components getTokenComponents() {
    String name = "Bearer Token";
    SecurityRequirement requirement = new SecurityRequirement().addList(name);
    return new Components()
        .addSecuritySchemes(name,
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .in(SecurityScheme.In.HEADER)
                .scheme(AuthProperty.ACCESS_TOKEN_PREFIX)
                .name(AuthProperty.ACCESS_TOKEN_HEADER)
                .bearerFormat("JWT"));
  }

  public SecurityRequirement getTokenSecurityRequirement() {
    String name = "Bearer Token";
    return new SecurityRequirement().addList(name);
  }
}
