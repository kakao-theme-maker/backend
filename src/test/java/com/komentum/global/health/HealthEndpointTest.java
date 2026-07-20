package com.komentum.global.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(HealthEndpointTest.HealthTestConfig.class)
class HealthEndpointTest {

  private static final AtomicBoolean HEALTHY = new AtomicBoolean(true);

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    HEALTHY.set(true);
  }

  @Test
  void health_successWithStatusOnly() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(content().string("{\"status\":\"UP\"}"));
  }

  @Test
  void health_failureWithStatusOnly() throws Exception {
    HEALTHY.set(false);

    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isServiceUnavailable())
        .andExpect(content().string("{\"status\":\"DOWN\"}"));
  }

  @TestConfiguration
  static class HealthTestConfig {

    @Bean
    HealthIndicator ciHealthIndicator() {
      return () -> HEALTHY.get() ? Health.up().build() : Health.down().build();
    }
  }
}
