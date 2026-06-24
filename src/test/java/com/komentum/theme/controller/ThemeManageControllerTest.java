package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.domain.DesignComponent;
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
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.user.domain.User;
import java.util.List;
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
  @DisplayName("")
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
  @DisplayName("")
  public void updateTheme_success() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
//    ThemeUpdateRequest request = ThemeUpdateRequest.builder()
//        .themeName(UUID.randomUUID().toString())
//        .styleCodes(Map.of(TypeCode.))
//        .typeCodes(Map.of())
//        .build();
//    // when
//    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/themes/{id}",
//            target.getThemeComponentId())
//        .contentType(MediaType.APPLICATION_JSON)
//        .content(objectMapper.writeValueAsString(createThemeRequest));
//    request = mockMvcUtils.addAuthentication(request, testUser.getPublicUserId());
//    // then
//    mockMvc.perform(request)
//        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  public void markThemeAsDone_success() throws Exception {
    // given
    ThemeComponent target = themeDataGenerator.initialThemes.get(0);
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
    ThemeComponent target = themeDataGenerator.initialThemes.get(0);
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/themes/{id}",
        target.getThemeComponentId());
    request = mockMvcUtils.addAuthentication(request, testUser.getPublicUserId());
    // then
    mockMvc.perform(request)
        .andExpect(status().isNoContent());
  }
}
