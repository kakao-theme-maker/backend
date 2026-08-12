package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.endsWith;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.utils.FileManager;
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
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeImageUpdateRequest;
import com.komentum.theme.core.dto.ThemeUpdateRequest.ThemeStyleUpdateRequest;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import com.komentum.theme.ios.dto.IosThemePackageResponse;
import com.komentum.user.domain.User;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
  @Autowired
  private FileManager fileManager;

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
  @DisplayName("테마 소유자는 iOS ktheme 패키지를 생성할 수 있다")
  public void makeIosThemePackage_owner_success() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
    String expectedUrl = "https://cdn.example.com/theme.ktheme";
    byte[] sampleImageBytes = Files.readAllBytes(
        Paths.get("src/test/resources/sample-images/test.png"));
    when(fileManager.convertUrlToFileName(anyString())).thenReturn("source.png");
    when(fileManager.downloadFile("source.png")).thenAnswer(invocation -> {
      assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
      return sampleImageBytes;
    });
    when(fileManager.uploadFile(any(byte[].class), endsWith(".ktheme"))).thenAnswer(invocation -> {
      assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
      return expectedUrl;
    });
    clearInvocations(fileManager);

    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
        "/api/themes/{id}/packages/ios", targetTheme.getThemeComponentId());
    ResultActions resultActions = mockMvcUtils.performAuthRequest(request,
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .clientDto(TestClientDto.fromEntity(testUser))
            .build());

    // then
    resultActions.andExpect(status().isOk());
    IosThemePackageResponse response = mockMvcUtils.parseResponse(resultActions,
        new TypeReference<>() {
        });
    assertThat(response.getThemeComponentId()).isEqualTo(targetTheme.getThemeComponentId());
    assertThat(response.getFileName()).startsWith("ios-theme-").endsWith(".ktheme");
    assertThat(response.getThemeUrl()).isEqualTo(expectedUrl);
    ArgumentCaptor<byte[]> packageCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(fileManager).uploadFile(packageCaptor.capture(), anyString());
    assertThat(readZipEntryNames(packageCaptor.getValue()))
        .contains(
            "KakaoTalkTheme.css",
            "Images/maintabIcoPiccoma@2x.png",
            "Images/maintabIcoPiccomaSelected@3x.png",
            "Images/chatroomBubbleSend01Selected@2x.png",
            "Images/chatroomBubbleReceive02Selected@3x.png"
        );
  }

  @Test
  @DisplayName("관리자는 다른 사용자의 테마도 iOS ktheme 패키지로 생성할 수 있다")
  public void makeIosThemePackage_admin_success() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
    User rootUser = userScenarioSupport.builder()
        .withRootUser()
        .build()
        .rootUser();
    String expectedUrl = "https://cdn.example.com/admin-theme.ktheme";
    byte[] sampleImageBytes = Files.readAllBytes(
        Paths.get("src/test/resources/sample-images/test.png"));
    when(fileManager.convertUrlToFileName(anyString())).thenReturn("source.png");
    when(fileManager.downloadFile("source.png")).thenReturn(sampleImageBytes);
    when(fileManager.uploadFile(any(byte[].class), anyString())).thenReturn(expectedUrl);

    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
        "/api/themes/{id}/packages/ios", targetTheme.getThemeComponentId());
    ResultActions resultActions = mockMvcUtils.performAuthRequest(request,
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .clientDto(TestClientDto.fromEntity(rootUser))
            .build());

    // then
    resultActions.andExpect(status().isOk());
  }

  @Test
  @DisplayName("테마 소유자가 아닌 사용자는 iOS ktheme 패키지를 생성할 수 없다")
  public void makeIosThemePackage_forbidden() throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport.builder(List.of(testUser),
            designComponentList)
        .withCountPerUser(1)
        .build().themeComponents().get(0);
    User otherUser = userScenarioSupport.builder()
        .withUsers(2)
        .build()
        .users()
        .stream()
        .filter(user -> !user.getPublicUserId().equals(testUser.getPublicUserId()))
        .findFirst()
        .orElseThrow();

    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
        "/api/themes/{id}/packages/ios", targetTheme.getThemeComponentId());
    request = mockMvcUtils.addAuthentication(request, TestClientDto.fromEntity(otherUser));

    // then
    mockMvc.perform(request)
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("존재하지 않는 테마의 iOS ktheme 패키지 생성 요청은 404를 반환한다")
  public void makeIosThemePackage_notFound() throws Exception {
    // given
    int missingThemeId = Integer.MAX_VALUE;
    clearInvocations(fileManager);

    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post(
        "/api/themes/{id}/packages/ios", missingThemeId);
    ResultActions resultActions = mockMvcUtils.performAuthRequest(request,
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .clientDto(TestClientDto.fromEntity(testUser))
            .build());

    // then
    resultActions
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Theme not found with id: " + missingThemeId));
    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
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
    List<ThemeComponent> themeComponents = themeComponentRepository.findAll();
    assertThat(themeComponents).hasSize(1);
    assertThat(themeComponents.get(0).getThemeCode())
        .isEqualTo("apeach_5031cb08-7ae9-40a7-a57c-2d24bd93f2d5");
    assertThat(themeImageRepository.count()).isEqualTo(TypeCode.values().length);
    assertThat(themeStyleRepository.count()).isEqualTo(StyleCode.values().length);
    resultActions.andExpect(status().isOk());
  }

  private Set<String> readZipEntryNames(byte[] zipBytes) throws Exception {
    Set<String> entryNames = new LinkedHashSet<>();
    try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
      ZipEntry entry = zipInputStream.getNextEntry();
      while (entry != null) {
        entryNames.add(entry.getName());
        zipInputStream.closeEntry();
        entry = zipInputStream.getNextEntry();
      }
    }
    return entryNames;
  }
}
