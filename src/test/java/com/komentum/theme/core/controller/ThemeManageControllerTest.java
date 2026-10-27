package com.komentum.theme.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.dto.ColorStyleUpdateRequest;
import com.komentum.designcomponent.dto.ComponentTypeUpdateRequest;
import com.komentum.designcomponent.enums.PlatformScope;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.designcomponent.service.ColorStyleService;
import com.komentum.designcomponent.service.ComponentTypeService;
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
import com.komentum.theme.core.dto.ThemeCloneRequest;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.InsetUpdateDto;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.user.domain.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
  @Autowired
  private DesignComponentRepository designComponentRepository;
  @Autowired
  private ComponentTypeService componentTypeService;
  @Autowired
  private ColorStyleService colorStyleService;
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
                .color("#ffffff").alpha(50).build()
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
  @Transactional
  @DisplayName("사용자는 이미지/색상 정보를 기반으로 테마를 복제할 수 있고, 복제된 테마의 제작자는 자기 자신이다")
  public void cloneTheme_success() throws Exception {
    // given
    ThemeComponent sourceTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    ThemeCloneRequest cloneRequest = buildCloneRequestFromTheme(sourceTheme.getThemeComponentId(),
        "cloned theme");
    // when
    ResultActions resultActions = doThemeCloneRequest(cloneRequest, testUser);
    // then
    resultActions.andExpect(status().isCreated());
    ThemeDetailResponse response = mockMvcUtils.parseResponse(resultActions, new TypeReference<>() {
    });
    assertThat(response.getThemeComponentId()).isNotEqualTo(sourceTheme.getThemeComponentId());
    assertThat(response.getThemeName()).isEqualTo("cloned theme");
    assertThat(response.getTypeCodes()).hasSize(TypeCode.values().length);
    assertThat(response.getStyleCodes()).hasSize(StyleCode.values().length);
    ThemeComponent clonedTheme = themeComponentRepository.findById(response.getThemeComponentId())
        .orElseThrow();
    assertThat(clonedTheme.getUser().getUserEmail()).isEqualTo(testUser.getUserEmail());
  }

  @Test
  @DisplayName("복제 요청에 TypeCode가 누락되면 테마를 복제할 수 없다")
  public void cloneTheme_failWhenTypeCodeMissing() throws Exception {
    // given
    ThemeComponent sourceTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    ThemeCloneRequest cloneRequest = buildCloneRequestFromTheme(sourceTheme.getThemeComponentId(),
        "cloned theme");
    Map<TypeCode, ThemeImageUpdateRequest> incompleteTypeCodes = new HashMap<>(
        cloneRequest.getTypeCodes());
    incompleteTypeCodes.remove(TypeCode.COMMON_ICO_THEME);
    cloneRequest.setTypeCodes(incompleteTypeCodes);
    // when
    ResultActions resultActions = doThemeCloneRequest(cloneRequest, testUser);
    // then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("복제 요청에 StyleCode가 누락되면 테마를 복제할 수 없다")
  public void cloneTheme_failWhenStyleCodeMissing() throws Exception {
    // given
    ThemeComponent sourceTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    ThemeCloneRequest cloneRequest = buildCloneRequestFromTheme(sourceTheme.getThemeComponentId(),
        "cloned theme");
    Map<StyleCode, ThemeStyleUpdateRequest> incompleteStyleCodes = new HashMap<>(
        cloneRequest.getStyleCodes());
    incompleteStyleCodes.remove(StyleCode.CHAT_ROOM_BACKGROUND_COLOR);
    cloneRequest.setStyleCodes(incompleteStyleCodes);
    // when
    ResultActions resultActions = doThemeCloneRequest(cloneRequest, testUser);
    // then
    resultActions.andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("platformScope가 COMMON이 아닌 TypeCode/StyleCode는 요청에 없어도 테마를 복제할 수 있다")
  public void cloneTheme_success_whenPlatformSpecificCodeIsMissing() throws Exception {
    // given
    ThemeComponent sourceTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    ComponentType androidOnlyComponentType = componentTypeService.findComponentTypeMap()
        .get(TypeCode.COMMON_ICO_THEME);
    componentTypeService.updateComponentType(androidOnlyComponentType.getComponentTypeId(),
        ComponentTypeUpdateRequest.builder()
            .name(androidOnlyComponentType.getName())
            .typeCode(TypeCode.COMMON_ICO_THEME)
            .platformScope(PlatformScope.ANDROID)
            .build());
    ColorStyle androidOnlyColorStyle = colorStyleService.findColorStyleMap()
        .get(StyleCode.CHAT_ROOM_BACKGROUND_COLOR);
    colorStyleService.updateColorStyle(androidOnlyColorStyle.getColorStyleId(),
        ColorStyleUpdateRequest.builder()
            .name(androidOnlyColorStyle.getName())
            .styleCode(StyleCode.CHAT_ROOM_BACKGROUND_COLOR)
            .platformScope(PlatformScope.ANDROID)
            .build());
    ThemeCloneRequest cloneRequest = buildCloneRequestFromTheme(sourceTheme.getThemeComponentId(),
        "cloned theme");
    Map<TypeCode, ThemeImageUpdateRequest> typeCodesWithoutPlatformSpecific = new HashMap<>(
        cloneRequest.getTypeCodes());
    typeCodesWithoutPlatformSpecific.remove(TypeCode.COMMON_ICO_THEME);
    cloneRequest.setTypeCodes(typeCodesWithoutPlatformSpecific);
    Map<StyleCode, ThemeStyleUpdateRequest> styleCodesWithoutPlatformSpecific = new HashMap<>(
        cloneRequest.getStyleCodes());
    styleCodesWithoutPlatformSpecific.remove(StyleCode.CHAT_ROOM_BACKGROUND_COLOR);
    cloneRequest.setStyleCodes(styleCodesWithoutPlatformSpecific);
    // when
    ResultActions resultActions = doThemeCloneRequest(cloneRequest, testUser);
    // then
    resultActions.andExpect(status().isCreated());
    ThemeDetailResponse response = mockMvcUtils.parseResponse(resultActions, new TypeReference<>() {
    });
    assertThat(response.getTypeCodes()).doesNotContainKey(TypeCode.COMMON_ICO_THEME);
    assertThat(response.getStyleCodes()).doesNotContainKey(StyleCode.CHAT_ROOM_BACKGROUND_COLOR);
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
  @DisplayName("소유자는 테마와 이미지·스타일 연결을 삭제하고 공유 에셋은 보존한다")
  public void deleteTheme_success() throws Exception {
    ThemeComponent target = createThemeForDeletion();

    doThemeDeleteRequest(target.getThemeComponentId(), testUser)
        .andExpect(status().isNoContent());

    assertThemeDeleted(target.getThemeComponentId());
    assertDesignComponentsPreserved();
  }

  /**
   * 관리자가 다른 사용자의 테마와 이미지·스타일 연결을 삭제할 수 있는지 검증한다.
   */
  @Test
  @DisplayName("관리자는 다른 사용자의 테마와 이미지·스타일 연결을 삭제한다")
  public void deleteTheme_successWhenAdminDeletesAnotherUsersTheme() throws Exception {
    ThemeComponent target = createThemeForDeletion();
    User admin = userScenarioSupport.builder().withRootUser().build().rootUser();

    doThemeDeleteRequest(target.getThemeComponentId(), admin)
        .andExpect(status().isNoContent());

    assertThemeDeleted(target.getThemeComponentId());
  }

  /**
   * 다른 사용자의 삭제 요청이 거부되고 테마와 연관 데이터가 보존되는지 검증한다.
   */
  @Test
  @DisplayName("다른 사용자의 테마 삭제 요청은 403을 반환하고 테마와 연결을 보존한다")
  public void deleteTheme_failWhenDeletingNotOwnedTheme() throws Exception {
    ThemeComponent target = createThemeForDeletion();
    Integer themeId = target.getThemeComponentId();
    List<Long> imageIds = themeImageRepository.fetchJoinAllByThemeComponentId(themeId).stream()
        .map(ThemeImage::getThemeImageId).toList();
    List<Long> styleIds = themeStyleRepository.fetchJoinAllByThemeComponentId(themeId).stream()
        .map(ThemeStyle::getThemeStyleId).toList();
    User other = userScenarioSupport.builder().withUsers(1).build().getFirstUser();

    doThemeDeleteRequest(themeId, other)
        .andExpect(status().isForbidden());

    assertThemePreserved(themeId, imageIds, styleIds);
  }

  /**
   * 비인증 삭제 요청이 거부되고 테마와 연관 데이터가 보존되는지 검증한다.
   */
  @Test
  @DisplayName("인증하지 않은 테마 삭제 요청은 401을 반환하고 테마와 연결을 보존한다")
  public void deleteTheme_failWhenUnauthenticated() throws Exception {
    ThemeComponent target = createThemeForDeletion();
    Integer themeId = target.getThemeComponentId();
    List<Long> imageIds = themeImageRepository.fetchJoinAllByThemeComponentId(themeId).stream()
        .map(ThemeImage::getThemeImageId).toList();
    List<Long> styleIds = themeStyleRepository.fetchJoinAllByThemeComponentId(themeId).stream()
        .map(ThemeStyle::getThemeStyleId).toList();

    mockMvc.perform(MockMvcRequestBuilders.delete("/api/themes/{id}", themeId))
        .andExpect(status().isUnauthorized());

    assertThemePreserved(themeId, imageIds, styleIds);
  }

  /**
   * 존재하지 않는 테마의 삭제 요청이 기존처럼 404를 반환하는지 검증한다.
   */
  @Test
  @DisplayName("존재하지 않는 테마 삭제 요청은 404를 반환한다")
  public void deleteTheme_failWhenThemeNotFound() throws Exception {
    doThemeDeleteRequest(Integer.MAX_VALUE, testUser)
        .andExpect(status().isNotFound());
  }

  /**
   * 삭제 검증에 사용할 이미지와 스타일이 연결된 소유자 테마를 생성한다.
   */
  private ThemeComponent createThemeForDeletion() {
    ThemeComponent theme = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
    assertThat(themeImageRepository.fetchJoinAllByThemeComponentId(theme.getThemeComponentId()))
        .isNotEmpty();
    assertThat(themeStyleRepository.fetchJoinAllByThemeComponentId(theme.getThemeComponentId()))
        .isNotEmpty();
    return theme;
  }

  /**
   * 지정한 사용자의 인증 정보로 테마 삭제 API를 호출한다.
   */
  private ResultActions doThemeDeleteRequest(Integer themeId, User client) throws Exception {
    return mockMvc.perform(mockMvcUtils.addAuthentication(
        MockMvcRequestBuilders.delete("/api/themes/{id}", themeId),
        TestClientDto.fromEntity(client)));
  }

  /**
   * 테마와 해당 테마의 이미지·스타일 연결이 모두 삭제되었는지 확인한다.
   */
  private void assertThemeDeleted(Integer themeId) {
    assertThat(themeComponentRepository.existsById(themeId)).isFalse();
    assertThat(themeImageRepository.fetchJoinAllByThemeComponentId(themeId)).isEmpty();
    assertThat(themeStyleRepository.fetchJoinAllByThemeComponentId(themeId)).isEmpty();
  }

  /**
   * 거절된 삭제 요청 이후 테마와 기존 이미지·스타일 연결이 보존되었는지 확인한다.
   */
  private void assertThemePreserved(Integer themeId, List<Long> imageIds, List<Long> styleIds) {
    assertThat(themeComponentRepository.existsById(themeId)).isTrue();
    assertThat(themeImageRepository.fetchJoinAllByThemeComponentId(themeId))
        .extracting(ThemeImage::getThemeImageId)
        .containsExactlyInAnyOrderElementsOf(imageIds);
    assertThat(themeStyleRepository.fetchJoinAllByThemeComponentId(themeId))
        .extracting(ThemeStyle::getThemeStyleId)
        .containsExactlyInAnyOrderElementsOf(styleIds);
  }

  /**
   * 테마가 참조하던 공유 에셋의 DB 행과 파일명이 보존되었는지 확인한다.
   */
  private void assertDesignComponentsPreserved() {
    for (DesignComponent component : designComponentList) {
      DesignComponent preserved = designComponentRepository.findById(component.getDesignComponentId())
          .orElseThrow();
      assertThat(preserved.getFileName()).isEqualTo(component.getFileName());
    }
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

  private ThemeCloneRequest buildCloneRequestFromTheme(Integer themeComponentId, String themeName) {
    Map<TypeCode, ThemeImageUpdateRequest> typeCodes = themeImageRepository
        .fetchJoinAllByThemeComponentId(themeComponentId).stream()
        .collect(Collectors.toMap(
            ti -> ti.getComponentType().getTypeCode(),
            ti -> ThemeImageUpdateRequest.builder()
                .designComponentId(ti.getDesignComponent().getDesignComponentId())
                .build()
        ));
    Map<StyleCode, ThemeStyleUpdateRequest> styleCodes = themeStyleRepository
        .fetchJoinAllByThemeComponentId(themeComponentId).stream()
        .collect(Collectors.toMap(
            ts -> ts.getColorStyle().getStyleCode(),
            ts -> ThemeStyleUpdateRequest.builder()
                .color(ts.getColor())
                .alpha(ts.getAlpha())
                .build()
        ));
    return ThemeCloneRequest.builder()
        .themeName(themeName)
        .typeCodes(typeCodes)
        .styleCodes(styleCodes)
        .build();
  }

  private ResultActions doThemeCloneRequest(ThemeCloneRequest dto, User client) throws Exception {
    return mockMvcUtils.performAuthRequest(
        MockMvcRequestBuilders.post("/api/themes/clone"),
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .body(dto)
            .clientDto(TestClientDto.fromEntity(client))
            .build());
  }
}
