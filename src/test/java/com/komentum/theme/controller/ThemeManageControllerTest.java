package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto.ExecutionContext;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class ThemeManageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private TestDataRemover testDataRemover;

  @Autowired
  private UserScenarioSupport userScenarioSupport;
  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;
  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;
  @Autowired
  private ThemeDataGenerator themeDataGenerator;
  @Autowired
  private ThemeImageRepository themeImageRepository;
  @Autowired
  private ThemeStyleRepository themeStyleRepository;
  @Autowired
  private ThemeComponentRepository themeComponentRepository;

  private User testUser;
  private List<DesignComponent> designComponentList;

  @BeforeEach
  void setUp() {
    var userResult = userScenarioSupport.builder()
        .withUsers(1).build();
    designComponentList = designComponentScenarioSupport.builder(userResult.users())
        .withCountPerUser(5)
        .build().designComponents();
    testUser = userResult.users().get(0);
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("새로운 테마 생성 시, 디폴트 테마를 복사해서 제공한다")
  public void createNewTheme_success() throws Exception {
    // given
    var themeResult = themeComponentScenarioSupport.builder(List.of(testUser), designComponentList)
        .withCountPerUser(1)
        .withDefaultTheme()
        .build();
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/themes");
    ResultActions resultActions = mockMvcUtils.performAuthRequest(request,
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .clientDto(TestClientDto.fromEntity(testUser))
            .build());
    // then
    resultActions.andExpect(status().isCreated());
    ThemeDetailResponse response = mockMvcUtils.parseResponse(resultActions, new TypeReference<>() {
    });
    assertThat(response.getThemeComponentId()).isNotNull();
    assertThat(response.getThemeName()).isNotBlank();
    assertThat(response.getTypeCodes()).isNotEmpty();
    assertThat(response.getStyleCodes()).isNotEmpty();
  }

  @Test
  @DisplayName("사용자는 테마 이름, 이미지, 색상을 수정할 수 있다")
  @Transactional
  public void updateTheme_success() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
    TypeCode updatedTypeCode = TypeCode.MAINVIEW_STYLE_PRIMARY_BACKGROUND_IMAGE;
    StyleCode updatedStyleCode = StyleCode.CHAT_ROOM_BACKGROUND_COLOR;
    String expectedThemeName = UUID.randomUUID().toString();
    DesignComponent expectedImage = designComponentList.get(0);
    String expectedColor = "#FFFFFF";
    ThemeUpdateRequest updateRequestDto = ThemeUpdateRequest.builder()
        .themeName(expectedThemeName)
        .typeCodes(Map.of(
            updatedTypeCode,
            ThemeImageUpdateRequest.builder()
                .designComponentId(expectedImage.getDesignComponentId())
                .build()
        ))
        .styleCodes(Map.of(
            updatedStyleCode,
            ThemeStyleUpdateRequest.builder()
                .color(expectedColor)
                .build()
        ))
        .build();
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/themes/{id}",
        targetTheme.getThemeComponentId());
    ResultActions resultActions = mockMvcUtils.performAuthRequest(request,
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .body(updateRequestDto)
            .clientDto(TestClientDto.fromEntity(testUser))
            .build());
    // then
    resultActions.andExpect(status().isNoContent());
    ThemeComponent updated = themeComponentRepository.findById(targetTheme.getThemeComponentId())
        .orElseThrow();
    ThemeImage updatedThemeImage = updated.getThemeImages().stream()
        .filter(v -> v.getComponentType().getTypeCode().equals(updatedTypeCode))
        .findFirst().orElseThrow();
    ThemeStyle updatedThemeStyle = updated.getThemeStyles().stream()
        .filter(v -> v.getColorStyle().getStyleCode().equals(updatedStyleCode))
        .findFirst().orElseThrow();
    assertThat(updated.getThemeName()).isEqualTo(expectedThemeName);
    assertThat(updatedThemeImage.getDesignComponent().getDesignComponentId())
        .isEqualTo(expectedImage.getDesignComponentId());
    assertThat(updatedThemeStyle.getColor()).isEqualTo(expectedColor);
  }

  @Test
  @DisplayName("")
  public void markThemeAsDone_success() throws Exception {
    // given
    ThemeComponent target = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/themes/{id}/done",
        target.getThemeComponentId());
    request = mockMvcUtils.addAuthentication(request, testUser.getPublicUserId());
    // then
    mockMvc.perform(request)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  public void deleteTheme_success() throws Exception {
    // given
    ThemeComponent target = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/themes/{id}",
        target.getThemeComponentId());
    request = mockMvcUtils.addAuthentication(request, testUser.getPublicUserId());
    // then
    mockMvc.perform(request)
        .andExpect(status().isNoContent());
  }
}
