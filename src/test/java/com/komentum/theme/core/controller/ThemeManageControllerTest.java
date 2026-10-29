package com.komentum.theme.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
import com.komentum.global.utils.FileManager;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeMetaDataScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto.ExecutionContext;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.fixture.theme.ThemeRequestFixture;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.dto.ThemeCloneRequest;
import com.komentum.theme.core.dto.ThemeCloneRequest.ThemeImageCloneRequest;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.theme.core.service.ThemeStyleService;
import com.komentum.user.domain.User;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
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
  @Autowired
  private DesignComponentRepository designComponentRepository;
  @Autowired
  private ComponentTypeService componentTypeService;
  @Autowired
  private ColorStyleService colorStyleService;
  @Autowired
  private DesignComponentRepository designComponentRepository;
  @MockitoBean
  private FileManager fileManager;
  @MockitoSpyBean
  private ThemeStyleService themeStyleService;
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
    // 기존 이미지는 샘플 PNG 파일로 다운로드된다
    when(fileManager.download(anyString())).thenAnswer(
        invocation -> new ClassPathResource("sample-images/test.png").getInputStream());
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
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId()).stream()
        .filter(image -> image.getComponentType().getTypeCode()
            == TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE)
        .findFirst().orElseThrow();
    Integer componentId = existingImage.getDesignComponent().getDesignComponentId();
    TypeCode typeCode = existingImage.getComponentType().getTypeCode();
    deactivateComponent(componentId);
    ThemeUpdateRequest updateDto = ThemeRequestFixture.insetUpdateRequest(typeCode, 30);

    doThemeUpdateRequest(targetTheme.getThemeComponentId(), updateDto, testUser)
        .andExpect(status().isNoContent());

    ThemeImage updatedImage = themeImageRepository
        .fetchJoinAllByThemeComponentId(targetTheme.getThemeComponentId()).stream()
        .filter(image -> image.getComponentType().getTypeCode() == typeCode)
        .findFirst().orElseThrow();
    assertThat(updatedImage.getDesignComponent().getDesignComponentId()).isEqualTo(componentId);
    assertThat(updatedImage.getDesignComponent().getIsActive()).isFalse();
    assertThat(updatedImage.getImageInset().getEdgeInsetTop()).isEqualTo(30);
    assertThat(updatedImage.getImageInset().getEdgeInsetBottom()).isEqualTo(30);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("기존 에셋의 활성 여부와 관계없이 파일로 교체하면 새 활성 에셋이 연결되고 기존 에셋과 파일은 보존된다")
  public void updateTheme_replaceImageWhenFileIsGiven(boolean inactive) throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE;
    DesignComponent previousComponent = findThemeImage(targetTheme, typeCode).getDesignComponent();
    Integer beforeDesignComponentId = previousComponent.getDesignComponentId();
    String beforeFileName = previousComponent.getFileName();
    if (inactive) {
      deactivateComponent(beforeDesignComponentId);
    }
    ThemeUpdateRequest updateDto = ThemeRequestFixture.insetUpdateRequest(typeCode, 50);
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        updateDto, testUser,
        MockMultipartFileUtils.generateSamplePngFormData(typeCode.getTypeCode()));
    // then
    resultActions.andExpect(status().isNoContent());
    ThemeImage updatedThemeImage = findThemeImage(targetTheme, typeCode);
    DesignComponent newDesignComponent = updatedThemeImage.getDesignComponent();
    assertThat(newDesignComponent.getDesignComponentId()).isNotEqualTo(beforeDesignComponentId);
    assertThat(newDesignComponent.getIsActive()).isTrue();
    assertThat(newDesignComponent.getFileName()).startsWith("design-components_").endsWith(".png");
    assertThat(updatedThemeImage.getImageInset().getEdgeInsetBottom()).isEqualTo(50);
    // 교체된 기존 DesignComponent는 공유될 수 있으므로 삭제하지 않는다
    DesignComponent preservedComponent = designComponentRepository.findById(beforeDesignComponentId)
        .orElseThrow();
    assertThat(preservedComponent.getFileName()).isEqualTo(beforeFileName);
    assertThat(preservedComponent.getIsActive()).isEqualTo(!inactive);
    verify(fileManager, never()).deleteFile(anyString());
  }

  @Test
  @DisplayName("이미지 파일 없이 이미지 정보만 전달하면 기존 이미지는 유지하고 inset 정보만 수정된다")
  public void updateTheme_updateInsetOnlyWhenFileIsNotGiven() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE;
    Integer beforeDesignComponentId = findThemeImage(targetTheme, typeCode)
        .getDesignComponent().getDesignComponentId();
    ThemeUpdateRequest updateDto = ThemeRequestFixture.insetUpdateRequest(typeCode, 30);
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        updateDto, testUser);
    // then
    resultActions.andExpect(status().isNoContent());
    ThemeImage updatedThemeImage = findThemeImage(targetTheme, typeCode);
    assertThat(updatedThemeImage.getDesignComponent().getDesignComponentId())
        .isEqualTo(beforeDesignComponentId);
    assertThat(updatedThemeImage.getImageInset().getEdgeInsetBottom()).isEqualTo(30);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  @DisplayName("에셋의 활성 여부와 관계없이 deleteTypeCodes에 포함된 TypeCode의 테마 이미지는 제거된다")
  public void updateTheme_deleteImageWhenTypeCodeInDeleteTypeCodes(boolean inactive) throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE;
    Integer componentId = findThemeImage(targetTheme, typeCode).getDesignComponent()
        .getDesignComponentId();
    if (inactive) {
      deactivateComponent(componentId);
    }
    ThemeUpdateRequest updateDto = ThemeRequestFixture.deleteUpdateRequest(typeCode);
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        updateDto, testUser);
    // then
    resultActions.andExpect(status().isNoContent());
    assertThat(findThemeImageMap(targetTheme)).doesNotContainKey(typeCode);
    assertThat(designComponentRepository.findById(componentId)).isPresent();
    verify(fileManager, never()).deleteFile(anyString());
  }

  @Test
  @DisplayName("파일도 없고 deleteTypeCodes에도 없는 TypeCode의 테마 이미지는 기존 이미지를 유지한다")
  public void updateTheme_keepImageWhenTypeCodeNotRequested() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    Map<TypeCode, Integer> beforeDesignComponentIds = findThemeImageMap(targetTheme).entrySet()
        .stream().collect(Collectors.toMap(Map.Entry::getKey,
            entry -> entry.getValue().getDesignComponent().getDesignComponentId()));
    TypeCode replaceTypeCode = TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE;
    TypeCode deleteTypeCode = TypeCode.CHAT_ROOM_BACKGROUND_IMAGE;
    ThemeUpdateRequest updateDto = ThemeRequestFixture.deleteUpdateRequest(deleteTypeCode);
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        updateDto, testUser,
        MockMultipartFileUtils.generateSamplePngFormData(replaceTypeCode.getTypeCode()));
    // then
    resultActions.andExpect(status().isNoContent());
    Map<TypeCode, ThemeImage> afterThemeImages = findThemeImageMap(targetTheme);
    assertThat(afterThemeImages).hasSize(beforeDesignComponentIds.size() - 1);
    beforeDesignComponentIds.forEach((typeCode, beforeDesignComponentId) -> {
      if (typeCode == replaceTypeCode || typeCode == deleteTypeCode) {
        return;
      }
      assertThat(afterThemeImages.get(typeCode).getDesignComponent().getDesignComponentId())
          .isEqualTo(beforeDesignComponentId);
    });
  }

  @Test
  @DisplayName("사용자는 테마 스타일을 수정할 수 있다")
  public void updateTheme_successWhenUpdateStyle() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    ThemeUpdateRequest updateDto = ThemeRequestFixture.styleUpdateRequest(
        StyleCode.CHAT_ROOM_BACKGROUND_COLOR, "#ffffff", 50);
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        updateDto, testUser);
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
  @DisplayName("사용자는 자신이 소유한 테마만 수정할 수 있고, 권한이 없으면 업로드 없이 403을 반환한다")
  public void updateTheme_failWhenUpdateNotOwnedTheme() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    User other = userScenarioSupport.builder()
        .withUsers(1)
        .build().users().get(0);
    ThemeUpdateRequest updateDto = ThemeRequestFixture.emptyUpdateRequest();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        updateDto, other, MockMultipartFileUtils.generateSamplePngFormData(
            TypeCode.CHAT_ROOM_BACKGROUND_IMAGE.getTypeCode()));
    // then
    resultActions.andExpect(status().isForbidden());
    verify(fileManager, never()).uploadPublicFile(any(InputStream.class), anyLong(), anyString());
  }

  @Test
  @DisplayName("교체하는 이미지 범위를 벗어난 inset이 전달되면 INVALID_IMAGE_INSET으로 400을 반환한다")
  public void updateTheme_failWhenInsetOutOfNewImageRange() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE;
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.insetUpdateRequest(typeCode, 100000), testUser,
        MockMultipartFileUtils.generateSamplePngFormData(typeCode.getTypeCode()));
    // then
    expectBadRequest(resultActions, "INVALID_IMAGE_INSET");
  }

  @Test
  @DisplayName("이미지 없이 기존 이미지 범위를 벗어난 inset이 전달되면 INVALID_IMAGE_INSET으로 400을 반환한다")
  public void updateTheme_failWhenInsetOutOfExistingImageRange() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE;
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.insetUpdateRequest(typeCode, 100000), testUser);
    // then
    expectBadRequest(resultActions, "INVALID_IMAGE_INSET");
    assertThat(findThemeImage(targetTheme, typeCode).getImageInset().getEdgeInsetBottom())
        .isEqualTo(15);
  }

  @Test
  @DisplayName("inset 없이 이미지만 교체해도 기존 inset이 새 이미지 범위를 벗어나면 INVALID_IMAGE_INSET으로 400을 반환한다")
  public void updateTheme_failWhenRetainedInsetOutOfNewImageRange() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE;
    // when ( 기존 inset의 stretch 좌표(50)보다 작은 이미지로 교체 )
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.emptyUpdateRequest(), testUser,
        MockMultipartFileUtils.generatePngFormData(typeCode.getTypeCode(), 10, 10));
    // then
    expectBadRequest(resultActions, "INVALID_IMAGE_INSET");
  }

  @Test
  @DisplayName("TypeCode에 없는 이름의 이미지 part가 있으면 UNKNOWN_TYPE_CODE로 400을 반환한다")
  public void updateTheme_failWhenUnknownTypeCode() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.emptyUpdateRequest(), testUser,
        MockMultipartFileUtils.generateSamplePngFormData("unknownImage"));
    // then
    expectBadRequest(resultActions, "UNKNOWN_TYPE_CODE");
  }

  @Test
  @DisplayName("같은 TypeCode의 이미지 part가 2개 이상이면 DUPLICATED_TYPE_CODE로 400을 반환한다")
  public void updateTheme_failWhenDuplicatedTypeCode() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.CHAT_ROOM_BACKGROUND_IMAGE;
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.emptyUpdateRequest(), testUser,
        MockMultipartFileUtils.generateSamplePngFormData(typeCode.getTypeCode()),
        MockMultipartFileUtils.generateSamplePngFormData(typeCode.getTypeCode()));
    // then
    expectBadRequest(resultActions, "DUPLICATED_TYPE_CODE");
  }

  @Test
  @DisplayName("빈 이미지 파일이 있으면 EMPTY_FILE로 400을 반환한다")
  public void updateTheme_failWhenEmptyFile() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.emptyUpdateRequest(), testUser,
        MockMultipartFileUtils.generateEmptyImageFormData(
            TypeCode.CHAT_ROOM_BACKGROUND_IMAGE.getTypeCode()));
    // then
    expectBadRequest(resultActions, "EMPTY_FILE");
  }

  @Test
  @DisplayName("같은 TypeCode가 이미지 파일과 deleteTypeCodes에 동시에 있으면 TYPE_CODE_CONFLICT로 400을 반환한다")
  public void updateTheme_failWhenTypeCodeConflict() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    TypeCode typeCode = TypeCode.CHAT_ROOM_BACKGROUND_IMAGE;
    ThemeUpdateRequest updateDto = ThemeRequestFixture.deleteUpdateRequest(typeCode);
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        updateDto, testUser,
        MockMultipartFileUtils.generateSamplePngFormData(typeCode.getTypeCode()));
    // then
    expectBadRequest(resultActions, "TYPE_CODE_CONFLICT");
  }

  @Test
  @DisplayName("Content-Type과 확장자가 이미지여도 실제 포맷이 이미지가 아니면 INVALID_IMAGE_FORMAT으로 400을 반환한다")
  public void updateTheme_failWhenInvalidImageFormat() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.emptyUpdateRequest(), testUser,
        MockMultipartFileUtils.generateImageFormData(
            TypeCode.CHAT_ROOM_BACKGROUND_IMAGE.getTypeCode(),
            "not an image".getBytes(StandardCharsets.UTF_8)));
    // then
    expectBadRequest(resultActions, "INVALID_IMAGE_FORMAT");
  }

  @Test
  @DisplayName("이미지 업로드 도중 실패하면 이번에 업로드한 파일을 모두 삭제하고, 테마 이미지는 변경되지 않는다")
  public void updateTheme_deleteUploadedFilesWhenUploadFails() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    Map<TypeCode, Integer> beforeDesignComponentIds = findDesignComponentIdMap(targetTheme);
    doReturn("uploaded-url")
        .doThrow(new RuntimeException("upload failed"))
        .when(fileManager).uploadPublicFile(any(InputStream.class), anyLong(), anyString());
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.emptyUpdateRequest(), testUser,
        MockMultipartFileUtils.generateSamplePngFormData(
            TypeCode.CHAT_ROOM_BACKGROUND_IMAGE.getTypeCode()),
        MockMultipartFileUtils.generateSamplePngFormData(
            TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE.getTypeCode()));
    // then
    resultActions.andExpect(status().isInternalServerError());
    ArgumentCaptor<String> uploadedFileNames = ArgumentCaptor.forClass(String.class);
    verify(fileManager, times(2)).uploadPublicFile(any(InputStream.class), anyLong(),
        uploadedFileNames.capture());
    String firstUploadedFileName = uploadedFileNames.getAllValues().get(0);
    verify(fileManager).deleteFile(firstUploadedFileName);
    assertThat(findDesignComponentIdMap(targetTheme)).isEqualTo(beforeDesignComponentIds);
  }

  @Test
  @DisplayName("트랜잭션 커밋에 실패하면 이번에 업로드한 파일을 삭제하고, 테마 이미지는 변경되지 않는다")
  public void updateTheme_deleteUploadedFilesWhenCommitFails() throws Exception {
    // given
    ThemeComponent targetTheme = createTargetTheme();
    Map<TypeCode, Integer> beforeDesignComponentIds = findDesignComponentIdMap(targetTheme);
    // 수정 트랜잭션 안에서 커밋 직전에 실패하는 synchronization을 등록한다
    doAnswer(invocation -> {
      invocation.callRealMethod();
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void beforeCommit(boolean readOnly) {
          throw new RuntimeException("commit failed");
        }
      });
      return null;
    }).when(themeStyleService).updateThemeStyles(anyInt(), any());
    // when
    ResultActions resultActions = doThemeUpdateRequest(targetTheme.getThemeComponentId(),
        ThemeRequestFixture.emptyUpdateRequest(), testUser,
        MockMultipartFileUtils.generateSamplePngFormData(
            TypeCode.CHAT_ROOM_BACKGROUND_IMAGE.getTypeCode()));
    // then
    resultActions.andExpect(status().isInternalServerError());
    ArgumentCaptor<String> uploadedFileName = ArgumentCaptor.forClass(String.class);
    verify(fileManager).uploadPublicFile(any(InputStream.class), anyLong(), uploadedFileName.capture());
    verify(fileManager).deleteFile(uploadedFileName.getValue());
    assertThat(findDesignComponentIdMap(targetTheme)).isEqualTo(beforeDesignComponentIds);
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
    Map<TypeCode, ThemeImageCloneRequest> incompleteTypeCodes = new HashMap<>(
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
    Map<TypeCode, ThemeImageCloneRequest> typeCodesWithoutPlatformSpecific = new HashMap<>(
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
    return ThemeRequestFixture.cloneRequest(themeName,
        themeImageRepository.fetchJoinAllByThemeComponentId(themeComponentId),
        themeStyleRepository.fetchJoinAllByThemeComponentId(themeComponentId));
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

  private ThemeComponent createTargetTheme() {
    return themeComponentScenarioSupport
        .builder(List.of(testUser), designComponentList)
        .withCountPerUser(1).build().themeComponents().get(0);
  }

  private Map<TypeCode, ThemeImage> findThemeImageMap(ThemeComponent theme) {
    return themeImageRepository.fetchJoinAllByThemeComponentId(theme.getThemeComponentId())
        .stream().collect(Collectors.toMap(ti -> ti.getComponentType().getTypeCode(), ti -> ti));
  }

  private Map<TypeCode, Integer> findDesignComponentIdMap(ThemeComponent theme) {
    return findThemeImageMap(theme).entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey,
            entry -> entry.getValue().getDesignComponent().getDesignComponentId()));
  }

  private ThemeImage findThemeImage(ThemeComponent theme, TypeCode typeCode) {
    ThemeImage themeImage = findThemeImageMap(theme).get(typeCode);
    assertThat(themeImage).isNotNull();
    return themeImage;
  }

  private void expectBadRequest(ResultActions resultActions, String errorCode) throws Exception {
    resultActions.andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(startsWith(errorCode)));
    verify(fileManager, never()).uploadPublicFile(any(InputStream.class), anyLong(), anyString());
  }

  private ResultActions doThemeUpdateRequest(Integer themeId, ThemeUpdateRequest dto, User client,
      MockMultipartFile... imageParts) throws Exception {
    MockMultipartHttpServletRequestBuilder request = MockMvcRequestBuilders.multipart(
        HttpMethod.PUT, "/api/themes/{id}", themeId);
    request.file(MockMultipartFileUtils.generateJsonBlobFormData("request", dto));
    for (MockMultipartFile imagePart : imageParts) {
      request.file(imagePart);
    }
    return mockMvc.perform(
        mockMvcUtils.addAuthentication(request, TestClientDto.fromEntity(client)));
  }
}
