package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeMetaDataScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto.ExecutionContext;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.InsetUpdateDto;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.user.domain.User;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
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
import org.springframework.transaction.support.TransactionSynchronizationManager;

@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@EnableTestProfile
@AutoConfigureMockMvc
class ThemeManageControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private MockMvcUtils mockMvcUtils;
  @Autowired
  private TestDataRemover testDataRemover;
  @Autowired
  private UserScenarioSupport userScenarioSupport;
  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;
  @Autowired
  private ThemeMetaDataScenarioSupport themeMetaDataScenarioSupport;
  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;
  @Autowired
  private ThemeComponentRepository themeComponentRepository;
  @Autowired
  private ThemeImageRepository themeImageRepository;
  @Autowired
  private ThemeStyleRepository themeStyleRepository;
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
    themeComponentScenarioSupport.builder(List.of(testUser), designComponentList)
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
  @DisplayName("사용자는 테마 이미지를 수정할 수 있다")
  public void updateTheme_successWhenUpdateImage() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    ThemeUpdateRequest updateDto = ThemeUpdateRequest.builder()
        .styleCodes(Map.of())
        .typeCodes(Map.of(
            TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE,
            ThemeImageUpdateRequest.builder()
                .designComponentId(designComponentList.get(0).getDesignComponentId())
                .inset(new InsetUpdateDto(50, 50, 50, 50, 50, 50))
                .build()
        ))
        .build();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto,
        testUser);
    // then
    resultActions.andExpect(status().isNoContent());
    ThemeImage updatedThemeImage = themeImageRepository.fetchJoinAllByThemeComponentId(
            targetTheme.getThemeComponentId())
        .stream().filter(v -> v.getComponentType().getTypeCode()
            .equals(TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE))
        .findFirst().orElseThrow();
    assertThat(updatedThemeImage.getImageInset().getEdgeInsetBottom()).isEqualTo(50);
  }

  @Test
  @DisplayName("테마 이미지에 null을 넣으면 해당 이미지를 삭제한다")
  public void updateTheme_deleteImageWhenImageIsNull() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    Map<TypeCode, ThemeImageUpdateRequest> updateMap = new HashMap<>();
    updateMap.put(TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE, null);
    ThemeUpdateRequest updateDto = ThemeUpdateRequest.builder()
        .styleCodes(Map.of())
        .typeCodes(updateMap)
        .build();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto,
        testUser);
    // then
    resultActions.andExpect(status().isNoContent());
    ThemeImage updatedThemeImage = themeImageRepository.fetchJoinAllByThemeComponentId(
            targetTheme.getThemeComponentId())
        .stream().filter(v -> v.getComponentType().getTypeCode()
            .equals(TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE))
        .findFirst().orElse(null);
    assertThat(updatedThemeImage).isNull();
  }

  @Test
  @DisplayName("사용자는 테마 스타일을 수정할 수 있다")
  public void updateTheme_successWhenUpdateStyle() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    ThemeUpdateRequest updateDto = ThemeUpdateRequest.builder()
        .styleCodes(Map.of(
            StyleCode.CHAT_ROOM_BACKGROUND_COLOR,
            ThemeStyleUpdateRequest.builder()
                .color("#ffffff").build()
        ))
        .typeCodes(Map.of())
        .build();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto,
        testUser);
    // then
    resultActions.andExpect(status().isNoContent());
    ThemeStyle updatedThemeStyle = themeStyleRepository.fetchJoinAllByThemeComponentId(
            targetTheme.getThemeComponentId())
        .stream()
        .filter(v -> v.getColorStyle().getStyleCode().equals(StyleCode.CHAT_ROOM_BACKGROUND_COLOR))
        .findFirst().orElseThrow();
    assertThat(updatedThemeStyle.getColor()).isEqualTo("#ffffff");
  }

  @Test
  @DisplayName("사용자는 자신이 소유한 테마만 수정할 수 있다")
  public void updateTheme_failWhenUpdateNotOwnedTheme() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    User other = userScenarioSupport.builder()
        .withUsers(1)
        .build().users().get(0);
    ThemeUpdateRequest updateDto = ThemeUpdateRequest.builder()
        .styleCodes(Map.of())
        .typeCodes(Map.of())
        .build();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto,
        other);
    // then
    resultActions.andExpect(status().isForbidden());
  }

  private ResultActions doThemeUpdateRequest(Integer themeId, ThemeUpdateRequest dto, User client)
      throws Exception {
    return mockMvcUtils.performAuthRequest(
        MockMvcRequestBuilders.put("/api/themes/{id}", themeId),
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .body(dto)
            .clientDto(TestClientDto.fromEntity(client))
            .build());
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
    request = mockMvcUtils.addAuthentication(request, TestClientDto.fromEntity(testUser));
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
    request = mockMvcUtils.addAuthentication(request, TestClientDto.fromEntity(testUser));
    // then
    mockMvc.perform(request)
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("루트 유저가 디폴트 테마를 생성한다")
  public void seedDefaultTheme_success() throws Exception {
    // given
    User rootUser = userScenarioSupport.builder().withRootUser().build().rootUser();
    themeMetaDataScenarioSupport.builder().withAll().build();
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/themes/default/seed");
    ResultActions resultActions = mockMvcUtils.performAuthRequest(request,
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .clientDto(TestClientDto.fromEntity(rootUser))
            .build());
    // then
    resultActions.andExpect(status().isOk());
    List<ThemeComponent> themeComponents = themeComponentRepository.findAll();
    assertThat(themeComponents).hasSize(1);
    assertThat(themeComponents.get(0).getThemeCode())
        .isEqualTo("apeach_5031cb08-7ae9-40a7-a57c-2d24bd93f2d5");
    assertThat(themeImageRepository.count()).isEqualTo(TypeCode.values().length);
    assertThat(themeStyleRepository.count()).isEqualTo(StyleCode.values().length);
    resultActions.andExpect(status().isOk());
  }
}
