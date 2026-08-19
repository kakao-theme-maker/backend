package com.komentum.theme.build.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.global.security.UserRole;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.fixture.theme.ThemeBuildFixture;
import com.komentum.theme.build.domain.ThemeBuildStatus;
import com.komentum.theme.build.dto.ThemeBuildStartRequest;
import com.komentum.theme.build.repository.ThemeBuildJobRepository;
import com.komentum.theme.build.service.ThemeBuildExecutionService;
import com.komentum.theme.build.service.ThemeBuildStateService;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class ThemeBuildControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private MockMvcUtils mockMvcUtils;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ThemeComponentRepository themeComponentRepository;
  @Autowired
  private ThemeBuildJobRepository themeBuildJobRepository;
  @Autowired
  private ThemeBuildStateService themeBuildStateService;

  @MockitoBean
  private ThemeBuildExecutionService themeBuildExecutionService;

  private User owner;
  private ThemeComponent theme;

  @BeforeEach
  void setUp() {
    owner = userRepository.save(
        ThemeBuildFixture.user("theme-build-owner@test.com", UserRole.USER));
    theme = themeComponentRepository.save(ThemeBuildFixture.theme(owner.getUserEmail()));
  }

  @AfterEach
  void tearDown() {
    themeBuildJobRepository.deleteAll();
    themeComponentRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @DisplayName("테마 제작을 시작하면 202와 RUNNING build 정보를 반환한다")
  void startThemeBuild_success() throws Exception {
    ResultActions result = performStart(theme, owner, Platform.ANDROID);

    result.andExpect(status().isAccepted())
        .andExpect(jsonPath("$.buildId").isNumber())
        .andExpect(jsonPath("$.themeComponentId").value(theme.getThemeComponentId()))
        .andExpect(jsonPath("$.platform").value("ANDROID"))
        .andExpect(jsonPath("$.status").value("RUNNING"));

    JsonNode body = readBody(result);
    assertThat(body.size()).isEqualTo(4);
    assertThat(themeBuildJobRepository.count()).isOne();
    verify(themeBuildExecutionService).dispatch(body.get("buildId").asLong());
  }

  @Test
  @DisplayName("iOS 테마 제작을 시작하면 202와 RUNNING build 정보를 반환한다")
  void startThemeBuild_iosSuccess() throws Exception {
    ResultActions result = performStart(theme, owner, Platform.IOS);

    result.andExpect(status().isAccepted())
        .andExpect(jsonPath("$.buildId").isNumber())
        .andExpect(jsonPath("$.themeComponentId").value(theme.getThemeComponentId()))
        .andExpect(jsonPath("$.platform").value("IOS"))
        .andExpect(jsonPath("$.status").value("RUNNING"));

    JsonNode body = readBody(result);
    assertThat(body.size()).isEqualTo(4);
    assertThat(themeBuildJobRepository.count()).isOne();
    assertThat(themeBuildJobRepository.findById(body.get("buildId").asLong()))
        .get()
        .extracting(job -> job.getPlatform())
        .isEqualTo(Platform.IOS);
    verify(themeBuildExecutionService).dispatch(body.get("buildId").asLong());
  }

  @Test
  @DisplayName("polling 응답은 RUNNING, SUCCESS, FAILED 상태와 downloadUrl을 제공한다")
  void findThemeBuild_returnsAllTerminalStates() throws Exception {
    Long runningBuildId = startAndReadBuildId(theme, owner);

    assertStatusResponse(performFind(runningBuildId, owner), "RUNNING", null);

    String packageUrl = "https://files.example.com/theme.apk";
    assertThat(themeBuildStateService.markSuccess(
        runningBuildId,
        packageUrl,
        LocalDateTime.now()
    )).isTrue();
    assertStatusResponse(performFind(runningBuildId, owner), "SUCCESS", packageUrl);

    Long failedBuildId = startAndReadBuildId(theme, owner);
    themeBuildStateService.markFailed(
        failedBuildId,
        LocalDateTime.now()
    );
    assertStatusResponse(performFind(failedBuildId, owner), "FAILED", null);
  }

  @Test
  @DisplayName("잘못된 플랫폼은 job을 만들지 않고 400을 반환한다")
  void startThemeBuild_invalidPlatform() throws Exception {
    mockMvc.perform(mockMvcUtils.addAuthentication(
            post("/api/themes/{themeComponentId}/builds", theme.getThemeComponentId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"platform\":\"WINDOWS\"}"),
            TestClientDto.fromEntity(owner)))
        .andExpect(status().isBadRequest());

    assertThat(themeBuildJobRepository.count()).isZero();
  }

  @Test
  @DisplayName("인증하지 않은 제작 요청은 401을 반환한다")
  void startThemeBuild_unauthorized() throws Exception {
    mockMvc.perform(post("/api/themes/{themeComponentId}/builds", theme.getThemeComponentId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ThemeBuildStartRequest(Platform.ANDROID))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("다른 사용자의 테마 제작 요청은 403을 반환한다")
  void startThemeBuild_forbidden() throws Exception {
    User otherUser = userRepository.save(
        ThemeBuildFixture.user("theme-build-other@test.com", UserRole.USER));

    performStart(theme, otherUser, Platform.ANDROID)
        .andExpect(status().isForbidden());

    assertThat(themeBuildJobRepository.count()).isZero();
  }

  @Test
  @DisplayName("관리자는 다른 사용자의 테마 제작을 시작할 수 있다")
  void startThemeBuild_adminSuccess() throws Exception {
    User admin = userRepository.save(
        ThemeBuildFixture.user("theme-build-start-admin@test.com", UserRole.ADMIN));

    performStart(theme, admin, Platform.ANDROID)
        .andExpect(status().isAccepted());

    assertThat(themeBuildJobRepository.count()).isOne();
  }

  @Test
  @DisplayName("없는 테마 제작 요청은 404를 반환한다")
  void startThemeBuild_notFound() throws Exception {
    mockMvc.perform(mockMvcUtils.addAuthentication(
            post("/api/themes/{themeComponentId}/builds", Integer.MAX_VALUE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new ThemeBuildStartRequest(Platform.ANDROID))),
            TestClientDto.fromEntity(owner)
        ))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("없는 build 조회는 404를 반환한다")
  void findThemeBuild_notFound() throws Exception {
    performFind(Long.MAX_VALUE, owner)
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("인증하지 않은 build 조회는 401을 반환한다")
  void findThemeBuild_unauthorized() throws Exception {
    Long buildId = startAndReadBuildId(theme, owner);

    mockMvc.perform(get("/api/theme-builds/{buildId}", buildId))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("다른 사용자의 build 조회는 403을 반환한다")
  void findThemeBuild_forbidden() throws Exception {
    Long buildId = startAndReadBuildId(theme, owner);
    User otherUser = userRepository.save(
        ThemeBuildFixture.user("theme-build-reader@test.com", UserRole.USER));

    performFind(buildId, otherUser)
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("관리자는 다른 사용자의 build를 조회할 수 있다")
  void findThemeBuild_adminSuccess() throws Exception {
    Long buildId = startAndReadBuildId(theme, owner);
    User admin = userRepository.save(
        ThemeBuildFixture.user("theme-build-admin@test.com", UserRole.ADMIN));

    assertStatusResponse(performFind(buildId, admin), "RUNNING", null);
  }

  private ResultActions performStart(
      ThemeComponent targetTheme,
      User client,
      Platform platform
  ) throws Exception {
    return mockMvc.perform(mockMvcUtils.addAuthentication(
        post("/api/themes/{themeComponentId}/builds", targetTheme.getThemeComponentId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(new ThemeBuildStartRequest(platform))),
        TestClientDto.fromEntity(client)
    ));
  }

  private ResultActions performFind(Long buildId, User client) throws Exception {
    return mockMvc.perform(mockMvcUtils.addAuthentication(
        get("/api/theme-builds/{buildId}", buildId),
        TestClientDto.fromEntity(client)
    ));
  }

  private Long startAndReadBuildId(ThemeComponent targetTheme, User client) throws Exception {
    ResultActions result = performStart(targetTheme, client, Platform.ANDROID)
        .andExpect(status().isAccepted());
    return readBody(result).get("buildId").asLong();
  }

  private void assertStatusResponse(
      ResultActions result,
      String expectedStatus,
      String expectedDownloadUrl
  ) throws Exception {
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(expectedStatus));
    JsonNode body = readBody(result);
    assertThat(body.size()).isEqualTo(2);
    assertThat(body.has("downloadUrl")).isTrue();
    if (expectedDownloadUrl == null) {
      assertThat(body.get("downloadUrl").isNull()).isTrue();
    } else {
      assertThat(body.get("downloadUrl").asText()).isEqualTo(expectedDownloadUrl);
    }
  }

  private JsonNode readBody(ResultActions result) throws Exception {
    return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
  }
}
