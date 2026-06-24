package com.komentum.designcomponent.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.dto.SeedResult;
import com.komentum.designcomponent.service.seeder.ComponentTypeSeeder;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class PlatformComponentTypeControllerTest {

  private final int PLATFORM_COMPONENT_TYPE_COUNT = 85;
  User rootUser;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private MockMvcUtils mockMvcUtils;
  @Autowired
  private UserScenarioSupport userScenarioSupport;
  @Autowired
  private ComponentTypeSeeder componentTypeSeeder;
  @Autowired
  private TestDataRemover testDataRemover;

  @BeforeEach
  public void setUp() {
    rootUser = userScenarioSupport.builder()
        .withRootUser()
        .build().rootUser();
    componentTypeSeeder.upsertComponentType();
  }

  @AfterEach
  public void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("when send request, create platform component types")
  public void seedData_success() throws Exception {
    // when
    SeedResult result = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/platform-component-types/seeds")
            .clientDto(TestClientDto.fromEntity(rootUser))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(result.getCreated()).isEqualTo(PLATFORM_COMPONENT_TYPE_COUNT);
    assertThat(result.getUpdated()).isEqualTo(0);
  }

  @Test
  @DisplayName("when send request twice, upsert platform component types")
  public void seedData_whenDoRequestTwice() throws Exception {
    // when
    SeedResult result1 = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/platform-component-types/seeds")
            .clientDto(TestClientDto.fromEntity(rootUser))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    SeedResult result2 = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/platform-component-types/seeds")
            .clientDto(TestClientDto.fromEntity(rootUser))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(result1.getCreated()).isEqualTo(PLATFORM_COMPONENT_TYPE_COUNT);
    assertThat(result1.getUpdated()).isEqualTo(0);
    assertThat(result2.getCreated()).isEqualTo(0);
    assertThat(result2.getUpdated()).isEqualTo(PLATFORM_COMPONENT_TYPE_COUNT);
  }
}
