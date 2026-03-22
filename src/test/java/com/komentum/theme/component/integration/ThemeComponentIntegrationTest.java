package com.komentum.theme.component.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.security.UserRole;
import com.komentum.global.utils.S3FileManager;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.data.DesignComponentDataGenerator;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateColorStyleRequest;
import com.komentum.theme.component.dto.CreateComponentTypeRequest;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateColorStyleRequest;
import com.komentum.theme.component.dto.UpdateComponentTypeRequest;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import com.komentum.user.redis.RedisSingleDataService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("Theme Component 통합 테스트")
class ThemeComponentIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ColorStyleRepository colorStyleRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @MockitoBean
  private S3FileManager s3FileManager;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @MockitoBean
  private RedisSingleDataService redisSingleDataService;
  @Autowired
  private DesignComponentDataGenerator designComponentDataGenerator;
  @Autowired
  private UserDataGenerator userDataGenerator;

  private User testUser;
  private TestClientDto testClient;

  @BeforeEach
  void setUp() {
    colorStyleRepository.deleteAll();
    componentTypeRepository.deleteAll();
    designComponentDataGenerator.deleteDesignComponents();
    userDataGenerator.deleteAllUsers();
    testUser = userDataGenerator.generateTestUser("test@example.com");
    testClient = TestClientDto.fromEntity(testUser);

    when(s3FileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn("https://s3.example.com/uploaded-image.png");

    CustomUserDetails userDetails = CustomUserDetails.builder()
        .userEmail(testUser.getUserEmail())
        .publicUserId(testUser.getPublicUserId())
        .userRole(UserRole.USER)
        .build();

    Authentication auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
    colorStyleRepository.deleteAll();
    componentTypeRepository.deleteAll();
    designComponentDataGenerator.deleteDesignComponents();
    userDataGenerator.deleteAllUsers();
  }

  @Nested
  @DisplayName("ColorStyle CRUD 테스트")
  class ColorStyleTests {

    @Test
    @DisplayName("ColorStyle 생성 테스트")
    void createColorStyle() throws Exception {
      // Given
      CreateColorStyleRequest request = CreateColorStyleRequest.builder()
          .explain("기본 색상 스타일")
          .platform(Platform.ANDROID)
          .styleSheetPath("/styles/colors.css")
          .styleElementName("primaryColor")
          .stylePropsName("color")
          .build();

      // When & Then
      mockMvc.perform(post("/api/color-styles")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("기본 색상 스타일"))
          .andExpect(jsonPath("$.platform").value("ANDROID"))
          .andExpect(jsonPath("$.styleSheetPath").value("/styles/colors.css"))
          .andExpect(jsonPath("$.styleElementName").value("primaryColor"))
          .andExpect(jsonPath("$.stylePropsName").value("color"));

      // 데이터베이스 검증
      assertThat(colorStyleRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("ColorStyle 조회 테스트")
    void getColorStyle() throws Exception {
      // Given
      ColorStyle savedColorStyle = colorStyleRepository.save(ColorStyle.builder()
          .explain("테스트 색상")
          .platform(Platform.ANDROID)
          .styleSheetPath("/test.css")
          .styleElementName("testColor")
          .stylePropsName("background-color")
          .build());

      // When & Then
      mockMvc.perform(get("/api/color-styles/{id}", savedColorStyle.getColorStyleId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.colorStyleId").value(savedColorStyle.getColorStyleId()))
          .andExpect(jsonPath("$.explain").value("테스트 색상"));
    }

    @Test
    @DisplayName("ColorStyle 전체 조회 테스트")
    void getAllColorStyles() throws Exception {
      // Given
      colorStyleRepository.save(ColorStyle.builder()
          .explain("색상1")
          .platform(Platform.ANDROID)
          .styleSheetPath("/test1.css")
          .styleElementName("color1")
          .stylePropsName("color")
          .build());

      colorStyleRepository.save(ColorStyle.builder()
          .explain("색상2")
          .platform(Platform.IOS)
          .styleSheetPath("/test2.css")
          .styleElementName("color2")
          .stylePropsName("background")
          .build());

      // When & Then
      mockMvc.perform(get("/api/color-styles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("ColorStyle 수정 테스트")
    void updateColorStyle() throws Exception {
      // Given
      ColorStyle savedColorStyle = colorStyleRepository.save(ColorStyle.builder()
          .explain("원본 색상")
          .platform(Platform.ANDROID)
          .styleSheetPath("/original.css")
          .styleElementName("originalColor")
          .stylePropsName("color")
          .build());

      UpdateColorStyleRequest updateRequest = UpdateColorStyleRequest.builder()
          .explain("수정된 색상")
          .platform(Platform.IOS)
          .styleSheetPath("/updated.css")
          .styleElementName("updatedColor")
          .stylePropsName("background-color")
          .build();

      // When & Then
      mockMvc.perform(put("/api/color-styles/{id}", savedColorStyle.getColorStyleId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("수정된 색상"))
          .andExpect(jsonPath("$.platform").value("IOS"));
    }

    @Test
    @DisplayName("ColorStyle 삭제 테스트")
    void deleteColorStyle() throws Exception {
      // Given
      ColorStyle savedColorStyle = colorStyleRepository.save(ColorStyle.builder()
          .explain("삭제될 색상")
          .platform(Platform.ANDROID)
          .styleSheetPath("/delete.css")
          .styleElementName("deleteColor")
          .stylePropsName("color")
          .build());

      // When & Then
      mockMvc.perform(delete("/api/color-styles/{id}", savedColorStyle.getColorStyleId()))
          .andExpect(status().isNoContent());

      // 데이터베이스 검증
      assertThat(colorStyleRepository.existsById(savedColorStyle.getColorStyleId())).isFalse();
    }
  }

  @Nested
  @DisplayName("ComponentType CRUD 테스트")
  class ComponentTypeTests {

    @Test
    @DisplayName("ComponentType 생성 테스트")
    void createComponentType() throws Exception {
      // Given
      CreateComponentTypeRequest request = CreateComponentTypeRequest.builder()
          .explain("버튼 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/components/button")
          .componentName("Button")
          .sizeX(100)
          .sizeY(50)
          .build();

      // When & Then
      mockMvc.perform(post("/api/component-types")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("버튼 컴포넌트"))
          .andExpect(jsonPath("$.componentName").value("Button"))
          .andExpect(jsonPath("$.sizeX").value(100))
          .andExpect(jsonPath("$.sizeY").value(50));

      assertThat(componentTypeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("ComponentType 조회 테스트")
    void getComponentType() throws Exception {
      // Given
      ComponentType savedComponent = componentTypeRepository.save(ComponentType.builder()
          .explain("테스트 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/test")
          .componentName("TestComponent")
          .sizeX(200)
          .sizeY(100)
          .build());

      // When & Then
      mockMvc.perform(get("/api/component-types/{id}", savedComponent.getComponentTypeId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.componentTypeId").value(savedComponent.getComponentTypeId()))
          .andExpect(jsonPath("$.componentName").value("TestComponent"));
    }

    @Test
    @DisplayName("ComponentType 전체 조회 테스트")
    void getAllComponentTypes() throws Exception {
      // Given
      componentTypeRepository.save(ComponentType.builder()
          .explain("컴포넌트1")
          .platform(Platform.ANDROID)
          .componentPath("/comp1")
          .componentName("Component1")
          .build());

      componentTypeRepository.save(ComponentType.builder()
          .explain("컴포넌트2")
          .platform(Platform.IOS)
          .componentPath("/comp2")
          .componentName("Component2")
          .build());

      // When & Then
      mockMvc.perform(get("/api/component-types"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("ComponentType 수정 테스트")
    void updateComponentType() throws Exception {
      // Given
      ComponentType savedComponent = componentTypeRepository.save(ComponentType.builder()
          .explain("원본 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/original")
          .componentName("OriginalComponent")
          .sizeX(100)
          .sizeY(50)
          .build());

      UpdateComponentTypeRequest updateRequest = UpdateComponentTypeRequest.builder()
          .explain("수정된 컴포넌트")
          .platform(Platform.IOS)
          .componentPath("/updated")
          .componentName("UpdatedComponent")
          .sizeX(150)
          .sizeY(75)
          .build();

      // When & Then
      mockMvc.perform(put("/api/component-types/{id}", savedComponent.getComponentTypeId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("수정된 컴포넌트"))
          .andExpect(jsonPath("$.componentName").value("UpdatedComponent"));
    }

    @Test
    @DisplayName("ComponentType 삭제 테스트")
    void deleteComponentType() throws Exception {
      // Given
      ComponentType savedComponent = componentTypeRepository.save(ComponentType.builder()
          .explain("삭제될 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/delete")
          .componentName("DeleteComponent")
          .build());

      // When & Then
      mockMvc.perform(delete("/api/component-types/{id}", savedComponent.getComponentTypeId()))
          .andExpect(status().isOk());

      assertThat(componentTypeRepository.existsById(savedComponent.getComponentTypeId())).isFalse();
    }
  }

  @Nested
  @DisplayName("DesignComponent CRUD 테스트")
  class DesignComponentTests {

    @Test
    @DisplayName("DesignComponent 생성 테스트")
    void createDesignComponent() throws Exception {
      // Given
      CreateDesignComponentRequest request = CreateDesignComponentRequest.builder()
          .isPublic(true)
          .build();

      MockMultipartFile image = new MockMultipartFile(
          "image", "test.png", "image/png", "test-image-content".getBytes());

      // When & Then
      MockHttpServletRequestBuilder requestBuilder = multipart("/api/design-components")
          .file(image)
          .param("isPublic", "true");

      MvcResult result = mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isOk())
          .andReturn();
      DesignComponentDto response = objectMapper.readValue(
          result.getResponse().getContentAsString(), DesignComponentDto.class);

      assertThat(response.getDesignComponentId()).isNotNull();
      assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
      assertThat(response.getIsPublic()).isTrue();
      assertThat(response.getCreatedAt()).isNotNull();
      assertThat(response.getUpdatedAt()).isNotNull();

      assertThat(designComponentRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("DesignComponent 조회 테스트")
    void getDesignComponent() throws Exception {
      // Given
      DesignComponent savedComponent = designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image.png", false);

      // When & Then
      MockHttpServletRequestBuilder requestBuilder = get("/api/design-components/{id}",
          savedComponent.getDesignComponentId());

      MvcResult result = mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isOk())
          .andReturn();
      DesignComponentDto response = objectMapper.readValue(
          result.getResponse().getContentAsString(), DesignComponentDto.class);

      assertThat(response.getDesignComponentId()).isEqualTo(savedComponent.getDesignComponentId());
      assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
      assertThat(response.getIsPublic()).isFalse();
      assertThat(response.getImageUrl()).isEqualTo("http://example.com/image.png");
      assertThat(response.getCreatedAt()).isNotNull();
      assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("DesignComponent 전체 조회 테스트")
    void getAllDesignComponents() throws Exception {
      // Given
      designComponentDataGenerator.generateData(5, 4);

      // When & Then
      MockHttpServletRequestBuilder requestBuilder = get("/api/design-components");

      mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(20));
    }


    @Test
    @DisplayName("DesignComponent 수정 테스트")
    void updateDesignComponent() throws Exception {
      // Given
      DesignComponent savedComponent = designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image.png", false);

      UpdateDesignComponentRequest updateRequest = UpdateDesignComponentRequest.builder()
          .isPublic(true)
          .build();
      MockMultipartFile image = new MockMultipartFile(
          "image", "updated.png", "image/png", "updated-image-content".getBytes());

      // When & Then
      MockHttpServletRequestBuilder requestBuilder = multipart(
          "/api/design-components/{id}", savedComponent.getDesignComponentId())
          .file(image)
          .param("isPublic", "true");

      requestBuilder.with(request -> {
        request.setMethod("PUT");
        return request;
      });

      MvcResult result = mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isOk())
          .andReturn();
      DesignComponentDto response = objectMapper.readValue(
          result.getResponse().getContentAsString(), DesignComponentDto.class);
      assertThat(response.getDesignComponentId()).isEqualTo(savedComponent.getDesignComponentId());
      assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
      assertThat(response.getIsPublic()).isTrue();
      assertThat(response.getImageUrl()).isNotBlank();
      assertThat(response.getImageUrl()).isNotEqualTo("http://example.com/image.png");
      assertThat(response.getImageUrl()).isEqualTo("https://s3.example.com/uploaded-image.png");
      assertThat(response.getCreatedAt()).isNotNull();
      assertThat(response.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자의 DesignComponent 수정 실패 테스트")
    void updateDesignComponentByOtherUser() throws Exception {
      // Given - 다른 사용자가 만든 컴포넌트
      User otherUser = userDataGenerator.generateTestUser("other@test.com");
      DesignComponent savedComponent = designComponentDataGenerator.generateDesignComponent(
          otherUser, "http://example.com/image.png", false);

      UpdateDesignComponentRequest updateRequest = UpdateDesignComponentRequest.builder()
          .isPublic(true)
          .build();

      MockMultipartFile image = new MockMultipartFile(
          "image", "updated.png", "image/png", "updated-image-content".getBytes());
      MockMultipartFile requestPart = new MockMultipartFile(
          "request", "", "application/json", objectMapper.writeValueAsBytes(updateRequest));

      // When & Then - testUser로 수정 시도 (실패해야 함)
      MockMultipartHttpServletRequestBuilder requestBuilder = multipart(
          "/api/design-components/{id}", savedComponent.getDesignComponentId())
          .file(image)
          .file(requestPart);

      requestBuilder.with(request -> {
        request.setMethod("PUT");
        return request;
      });

      // 수정 안됐는지 확인
      mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isForbidden());

      DesignComponent afterComponent = designComponentRepository.findById(
          savedComponent.getDesignComponentId()).get();
      assertThat(afterComponent.getImageUrl()).isEqualTo("http://example.com/image.png");
      assertThat(afterComponent.getIsPublic()).isFalse();

    }


    @Test
    @DisplayName("DesignComponent 삭제 테스트")
    void deleteDesignComponent() throws Exception {
      // Given
      DesignComponent savedComponent = designComponentDataGenerator
          .generateDesignComponent(testUser);

      // When & Then
      MockHttpServletRequestBuilder requestBuilder =
          delete("/api/design-components/{id}", savedComponent.getDesignComponentId());

      mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isNoContent());

      assertThat(designComponentRepository.existsById(savedComponent.getDesignComponentId()))
          .isFalse();
    }


    @Test
    @DisplayName("다른 사용자의 DesignComponent 삭제 실패 테스트")
    void deleteDesignComponentByOtherUser() throws Exception {
      // Given - 다른 사용자가 만든 컴포넌트
      User otherUser = userDataGenerator.generateTestUser("other@test.com");
      DesignComponent savedComponent = designComponentDataGenerator
          .generateDesignComponent(otherUser, "https://other.com/image.png", false);

      // When & Then - testUser로 삭제 시도 (실패해야 함)
      MockHttpServletRequestBuilder requestBuilder =
          delete("/api/design-components/{id}", savedComponent.getDesignComponentId());

      mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isForbidden());

      // 삭제 안됐는지 확인
      assertThat(designComponentRepository.existsById(savedComponent.getDesignComponentId()))
          .isTrue();
    }
  }


  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationScenarioTests {

    @Test
    @DisplayName("전체 컴포넌트 생성 및 연동 테스트")
    void createAndLinkAllComponents() throws Exception {
      // ColorStyle 생성
      CreateColorStyleRequest colorStyleRequest = CreateColorStyleRequest.builder()
          .explain("통합 테스트 색상")
          .platform(Platform.ANDROID)
          .styleSheetPath("/integration.css")
          .styleElementName("integrationColor")
          .stylePropsName("color")
          .build();

      mockMvc.perform(post("/api/color-styles")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(colorStyleRequest)))
          .andExpect(status().isOk());

      // ComponentType 생성
      CreateComponentTypeRequest componentTypeRequest = CreateComponentTypeRequest.builder()
          .explain("통합 테스트 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/integration")
          .componentName("IntegrationComponent")
          .sizeX(300)
          .sizeY(200)
          .build();

      mockMvc.perform(post("/api/component-types")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(componentTypeRequest)))
          .andExpect(status().isOk());

      // DesignComponent 생성
      CreateDesignComponentRequest designComponentRequest = CreateDesignComponentRequest.builder()
          //.userEmail("integration@test.com")
          .isPublic(true)
          .build();

      mockMvc.perform(post("/api/design-components")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(designComponentRequest)))
          .andExpect(status().isOk());

      // 데이터베이스 검증
      assertThat(colorStyleRepository.count()).isEqualTo(1);
      assertThat(componentTypeRepository.count()).isEqualTo(1);
      assertThat(designComponentRepository.count()).isEqualTo(1);
    }
  }
}