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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
  @DisplayName("비활성 에셋이 포함된 기본 테마를 복사할 수 없고 새 테마가 남지 않는다")
  void createNewTheme_failWhenDefaultThemeContainsInactiveComponent() throws Exception {
    themeComponentScenarioSupport.builder(List.of(testUser),
            List.of(designComponentList.get(0)))
        .withCountPerUser(1)
        .withDefaultTheme()
        .build();
    deactivateComponent(designComponentList.get(0).getDesignComponentId());
    long themeCount = themeComponentRepository.count();
    long imageCount = themeImageRepository.count();
    long styleCount = themeStyleRepository.count();

    mockMvcUtils.performAuthRequest(MockMvcRequestBuilders.post("/api/themes"),
            ExecutionContext.builder()
                .mockMvc(mockMvc)
                .clientDto(TestClientDto.fromEntity(testUser))
                .build())
        .andExpect(status().isNotFound());

    assertThat(themeComponentRepository.count()).isEqualTo(themeCount);
    assertThat(themeImageRepository.count()).isEqualTo(imageCount);
    assertThat(themeStyleRepository.count()).isEqualTo(styleCount);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("완성 또는 작성 중 테마에서 기존 비활성 에셋을 유지하면서 inset을 수정할 수 있다")
  void updateTheme_successWhenRetainingInactiveComponent(boolean isDone) throws Exception {
    ThemeComponent targetTheme = createThemeUsing(designComponentList.get(0));
    targetTheme.setIsDone(isDone);
    themeComponentRepository.saveAndFlush(targetTheme);
    ThemeImage existingImage = themeImageRepository
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId()).get(0);
    Integer componentId = existingImage.getDesignComponent().getDesignComponentId();
    TypeCode typeCode = existingImage.getComponentType().getTypeCode();
    deactivateComponent(componentId);
    ThemeUpdateRequest updateDto = ThemeUpdateRequest.builder()
        .typeCodes(Map.of(typeCode, ThemeImageUpdateRequest.builder()
            .designComponentId(componentId)
            .inset(new InsetUpdateDto(10, 20, 50, 40, 60, 70))
            .build()))
        .build();

    doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto, testUser)
        .andExpect(status().isNoContent());

    ThemeImage updatedImage = themeImageRepository
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId()).stream()
        .filter(image -> image.getComponentType().getTypeCode() == typeCode)
        .findFirst().orElseThrow();
    assertThat(updatedImage.getDesignComponent().getDesignComponentId()).isEqualTo(componentId);
    assertThat(updatedImage.getDesignComponent().getIsActive()).isFalse();
    assertThat(updatedImage.getImageInset().getEdgeInsetTop()).isEqualTo(10);
    assertThat(updatedImage.getImageInset().getEdgeInsetBottom()).isEqualTo(50);
  }

  @Test
  @DisplayName("새 비활성 에셋으로 교체하면 테마 메타데이터와 기존 연결도 유지된다")
  void updateTheme_failWhenReplacingWithInactiveComponent() throws Exception {
    ThemeComponent targetTheme = createThemeUsing(designComponentList.get(0));
    ThemeImage existingImage = themeImageRepository
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId()).get(0);
    Integer existingComponentId = existingImage.getDesignComponent().getDesignComponentId();
    Integer inactiveComponentId = designComponentList.get(1).getDesignComponentId();
    deactivateComponent(inactiveComponentId);
    ThemeUpdateRequest updateDto = ThemeUpdateRequest.builder()
        .themeName("must not persist")
        .typeCodes(Map.of(existingImage.getComponentType().getTypeCode(),
            ThemeImageUpdateRequest.builder().designComponentId(inactiveComponentId).build()))
        .build();

    doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto, testUser)
        .andExpect(status().isNotFound());

    ThemeComponent unchangedTheme = themeComponentRepository
        .findById(targetTheme.getThemeComponentId()).orElseThrow();
    assertThat(unchangedTheme.getThemeName()).isEqualTo(targetTheme.getThemeName());
    assertThat(unchangedTheme.getVersionNumber()).isEqualTo(targetTheme.getVersionNumber());
    assertThat(themeImageRepository.fetchJoinAllByThemeComponentId(
            targetTheme.getThemeComponentId()))
        .allSatisfy(image -> assertThat(image.getDesignComponent().getDesignComponentId())
            .isEqualTo(existingComponentId));
  }

  @Test
  @DisplayName("다른 슬롯에 쓰던 비활성 에셋을 재사용할 수 없고 다른 수정도 롤백한다")
  void updateTheme_failWhenReusingInactiveComponentForAnotherTypeCode() throws Exception {
    ThemeComponent targetTheme = createThemeUsing(designComponentList.get(0));
    List<ThemeImage> existingImages = themeImageRepository
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId());
    ThemeImage retainedImage = existingImages.get(0);
    ThemeImage replacedImage = existingImages.get(1);
    Integer inactiveComponentId = retainedImage.getDesignComponent().getDesignComponentId();
    Integer activeComponentId = designComponentList.get(1).getDesignComponentId();
    replacedImage.setDesignComponent(designComponentList.get(1));
    themeImageRepository.saveAndFlush(replacedImage);
    deactivateComponent(inactiveComponentId);
    Map<TypeCode, ThemeImageUpdateRequest> typeCodes = new LinkedHashMap<>();
    typeCodes.put(retainedImage.getComponentType().getTypeCode(), ThemeImageUpdateRequest.builder()
        .designComponentId(inactiveComponentId)
        .inset(new InsetUpdateDto(10, 20, 50, 40, 60, 70))
        .build());
    typeCodes.put(existingImages.get(2).getComponentType().getTypeCode(), null);
    typeCodes.put(replacedImage.getComponentType().getTypeCode(), ThemeImageUpdateRequest.builder()
        .designComponentId(inactiveComponentId).build());
    ThemeUpdateRequest updateDto = ThemeUpdateRequest.builder().typeCodes(typeCodes).build();

    doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto, testUser)
        .andExpect(status().isNotFound());

    Map<TypeCode, ThemeImage> unchangedImages = themeImageRepository
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId()).stream()
        .collect(Collectors.toMap(image -> image.getComponentType().getTypeCode(), image -> image));
    assertThat(unchangedImages).hasSize(existingImages.size());
    assertThat(unchangedImages.get(retainedImage.getComponentType().getTypeCode()).getImageInset())
        .usingRecursiveComparison().isEqualTo(retainedImage.getImageInset());
    assertThat(unchangedImages.get(replacedImage.getComponentType().getTypeCode())
        .getDesignComponent().getDesignComponentId()).isEqualTo(activeComponentId);
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
  @DisplayName("비활성 에셋을 사용하던 테마 이미지도 null을 넣으면 삭제한다")
  public void updateTheme_deleteImageWhenImageIsNull() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
    ThemeImage inactiveImage = themeImageRepository
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId()).stream()
        .filter(image -> image.getComponentType().getTypeCode()
            == TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE)
        .findFirst().orElseThrow();
    deactivateComponent(inactiveImage.getDesignComponent().getDesignComponentId());
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
  @DisplayName("테마 이미지를 모두 제거한 뒤 비활성 에셋을 다시 연결할 수 없다")
  void updateTheme_failWhenReattachingInactiveComponentAfterRemovingAllImages() throws Exception {
    ThemeComponent targetTheme = createThemeUsing(designComponentList.get(0));
    Integer themeId = targetTheme.getThemeComponentId();
    Integer componentId = designComponentList.get(0).getDesignComponentId();
    List<ThemeImage> existingImages = themeImageRepository.fetchJoinAllByThemeComponentId(themeId);
    TypeCode previousTypeCode = existingImages.get(0).getComponentType().getTypeCode();
    deactivateComponent(componentId);
    Map<TypeCode, ThemeImageUpdateRequest> removals = new HashMap<>();
    existingImages.forEach(image -> removals.put(image.getComponentType().getTypeCode(), null));

    doThemeUpdateRequest(themeId, ThemeUpdateRequest.builder().typeCodes(removals).build(), testUser)
        .andExpect(status().isNoContent());
    assertThat(themeImageRepository.fetchJoinAllByThemeComponentId(themeId)).isEmpty();
    String versionAfterRemoval = themeComponentRepository.findById(themeId).orElseThrow()
        .getVersionNumber();
    ResultActions detailResult = mockMvc.perform(MockMvcRequestBuilders.get("/api/themes/{id}",
            themeId))
        .andExpect(status().isOk());
    ThemeDetailResponse detail = mockMvcUtils.parseResponse(detailResult, new TypeReference<>() {
    });
    assertThat(detail.getTypeCodes()).isEmpty();

    ThemeUpdateRequest reattachment = ThemeUpdateRequest.builder()
        .typeCodes(Map.of(previousTypeCode, ThemeImageUpdateRequest.builder()
            .designComponentId(componentId).build()))
        .build();
    doThemeUpdateRequest(themeId, reattachment, testUser).andExpect(status().isNotFound());

    assertThat(themeImageRepository.fetchJoinAllByThemeComponentId(themeId)).isEmpty();
    assertThat(themeComponentRepository.findById(themeId).orElseThrow().getVersionNumber())
        .isEqualTo(versionAfterRemoval);
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
  @DisplayName("비활성 에셋이 포함된 테마는 복제할 수 없고 새 테마가 남지 않는다")
  void cloneTheme_failWhenComponentIsInactive() throws Exception {
    ThemeComponent sourceTheme = createThemeUsing(designComponentList.get(0));
    ThemeCloneRequest cloneRequest = buildCloneRequestFromTheme(sourceTheme.getThemeComponentId(),
        "cloned theme");
    deactivateComponent(designComponentList.get(0).getDesignComponentId());
    long themeCount = themeComponentRepository.count();
    long imageCount = themeImageRepository.count();
    long styleCount = themeStyleRepository.count();

    doThemeCloneRequest(cloneRequest, testUser).andExpect(status().isNotFound());

    assertThat(themeComponentRepository.count()).isEqualTo(themeCount);
    assertThat(themeImageRepository.count()).isEqualTo(imageCount);
    assertThat(themeStyleRepository.count()).isEqualTo(styleCount);
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

  private ThemeComponent createThemeUsing(DesignComponent component) {
    return themeComponentScenarioSupport.builder(List.of(testUser), List.of(component))
        .withCountPerUser(1).build().themeComponents().get(0);
  }

  private void deactivateComponent(Integer componentId) throws Exception {
    mockMvcUtils.performAuthRequest(
            MockMvcRequestBuilders.delete("/api/design-components/{id}", componentId),
            ExecutionContext.builder()
                .mockMvc(mockMvc)
                .clientDto(TestClientDto.fromEntity(testUser))
                .build())
        .andExpect(status().isNoContent());
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
