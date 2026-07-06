package com.komentum.designcomponent.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.dto.SeedResult;
import com.komentum.designcomponent.service.seeder.ColorStyleSeeder;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
import org.junit.jupiter.api.AfterEach;
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
class PlatformColorStyleControllerTest {

  private final int PLATFORM_COLOR_STYLE_COUNT = 69;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private MockMvcUtils mockMvcUtils;
  @Autowired
  private UserScenarioSupport userScenarioSupport;
  @Autowired
  private ColorStyleSeeder colorStyleSeeder;
  @Autowired
  private TestDataRemover testDataRemover;

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("when send request, generate data")
  public void seedData_success() throws Exception {
    // given
    User client = userScenarioSupport.builder()
        .withRootUser()
        .build()
        .rootUser();
    colorStyleSeeder.upsertColorStyleSeed();
    // when
    SeedResult result = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/platform-color-styles/seeds")
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(result.getCreated()).isEqualTo(PLATFORM_COLOR_STYLE_COUNT);
    assertThat(result.getUpdated()).isEqualTo(0);
  }

  @Test
  @DisplayName("when send request and data already exists, upsert data")
  public void seedData_whenDoRequestTwice() throws Exception {
    // given
    User client = userScenarioSupport.builder()
        .withRootUser()
        .build().rootUser();
    colorStyleSeeder.upsertColorStyleSeed();
    // when
    SeedResult result1 = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/platform-color-styles/seeds")
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    SeedResult result2 = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, SeedResult>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.PUT)
            .path("/api/platform-color-styles/seeds")
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(result1.getCreated()).isEqualTo(PLATFORM_COLOR_STYLE_COUNT);
    assertThat(result1.getUpdated()).isEqualTo(0);
    assertThat(result2.getCreated()).isEqualTo(0);
    assertThat(result2.getUpdated()).isEqualTo(PLATFORM_COLOR_STYLE_COUNT);
  }
}
