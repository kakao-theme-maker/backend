package gateway.com;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {
    private static final String USER_SERVICE_URL = "lb://USER-SERVICE";
    private static final String THEME_SERVICE_URL = "lb://THEME-SERVICE";

    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", r -> r
                        .path("/users/**")
                        .uri(USER_SERVICE_URL))
                .route("theme-service", r -> r
                        .path("/api/**")
                        .uri(THEME_SERVICE_URL))
                .build();
    }
}
