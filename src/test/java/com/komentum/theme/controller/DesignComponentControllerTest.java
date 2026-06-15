package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.security.UserRole;
import com.komentum.global.utils.FileManager;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.data.DesignComponentDataGenerator;
import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.test.data.MockMultipartFileUtils.ImageExtension;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.enums.TypeCode;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
@DisplayName("Design Component 테스트")
public class DesignComponentControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @MockitoBean
  private FileManager fileManager;

  @Autowired
  private DesignComponentDataGenerator designComponentDataGenerator;

  @Autowired
  private UserDataGenerator userDataGenerator;

  private User testUser;
  private TestClientDto testClient;
  private ComponentType componentTypeA;
  private ComponentType componentTypeB;


  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
    testUser = userDataGenerator.generateTestUser("test@example.com");
    testClient = TestClientDto.fromEntity(testUser);
    componentTypeRepository.deleteAll();
    componentTypeA = createComponentType("comp-a");
    componentTypeB = createComponentType("comp-b");

    when(fileManager.uploadFile(any(byte[].class), anyString()))
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
    designComponentDataGenerator.deleteDesignComponents();
    componentTypeRepository.deleteAll();
    userDataGenerator.deleteAllUsers();
  }

  private ComponentType createComponentType(String suffix) {
    String fileName = "theme_maintab_cell_image_" + suffix + ".png";
    return componentTypeRepository.save(ComponentType.builder()
        .explain("explain")
        .name("test component type")
        .typeCode(TypeCode.MAINVIEW_STYLE_PRIMARY_BACKGROUND_IMAGE)
        .build());
  }

  private MockMultipartFile createRequestPart(Object request) {
    return MockMultipartFileUtils.generateJsonFormData("request", request);
  }

  private MockMultipartFile createImagePart() {
    return MockMultipartFileUtils.generateImageFormData("image", ImageExtension.PNG);
  }

  @Nested
  @DisplayName("DesignComponent CRUD 테스트")
  class DesignComponentTests {

    @Test
    @DisplayName("DesignComponent 생성 테스트")
    void createDesignComponent() throws Exception {
      // Given
      CreateDesignComponentRequest createRequest = CreateDesignComponentRequest.builder()
          .isPublic(true)
          .componentTypeIds(
              List.of(componentTypeA.getComponentTypeId(), componentTypeB.getComponentTypeId()))
          .build();
      MockMultipartFile requestPart = createRequestPart(createRequest);
      MockMultipartFile image = createImagePart();

      // When & Then
      DesignComponentDto response = mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<DesignComponentDto>builder()
              .mockMvc(mockMvc)
              .path("/api/design-components")
              .httpMethod(HttpMethod.POST)
              .formDataList(List.of(requestPart, image))
              .clientDto(testClient)
              .statusCode(200)
              .responseType(new TypeReference<>() {
              })
              .build()
      );

      assertThat(response.getDesignComponentId()).isNotNull();
      assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
      assertThat(response.getIsPublic()).isTrue();
      assertThat(response.getCreatedAt()).isNotNull();
      assertThat(response.getUpdatedAt()).isNotNull();
      assertThat(response.getComponentTypes())
          .extracting("componentTypeId")
          .containsExactlyInAnyOrder(componentTypeA.getComponentTypeId(),
              componentTypeB.getComponentTypeId());

      assertThat(designComponentRepository.count()).isEqualTo(1);
      DesignComponent saved = designComponentRepository.findByDesignComponentId(
          response.getDesignComponentId()).orElseThrow();
      assertThat(saved.getComponentTypes())
          .extracting(ComponentType::getComponentTypeId)
          .containsExactlyInAnyOrder(componentTypeA.getComponentTypeId(),
              componentTypeB.getComponentTypeId());
    }

    @Test
    @DisplayName("DesignComponent 조회 테스트")
    void getDesignComponent() throws Exception {
      // Given
      DesignComponent savedComponent = designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image.png", false, List.of(componentTypeA, componentTypeB));

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
      assertThat(response.getComponentTypes())
          .extracting("componentTypeId")
          .containsExactlyInAnyOrder(componentTypeA.getComponentTypeId(),
              componentTypeB.getComponentTypeId());
    }

    @Test
    @DisplayName("DesignComponent 전체 조회 테스트")
    void getAllDesignComponents() throws Exception {
      // Given
      designComponentDataGenerator.generateDesignComponents(
          userDataGenerator.generateTestUsers(5), 4, List.of(componentTypeA));

      // When & Then
      MockHttpServletRequestBuilder requestBuilder = get("/api/design-components");

      mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content.length()").value(20))
          .andExpect(jsonPath("$.content[0].component_types.length()").value(1));
    }

    @Test
    @DisplayName("componentTypeId로 DesignComponent 목록 조회 테스트")
    void getDesignComponentsByComponentTypeId() throws Exception {
      // Given
      DesignComponent componentAOnly = designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image-a.png", true, List.of(componentTypeA));
      DesignComponent componentAAndB = designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image-ab.png", true,
          List.of(componentTypeA, componentTypeB));
      designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image-b.png", true, List.of(componentTypeB));

      // When
      MockHttpServletRequestBuilder requestBuilder = get(
          "/api/design-components/component-types/{componentTypeId}",
          componentTypeA.getComponentTypeId());

      MvcResult result = mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isOk())
          .andReturn();

      // Then
      DesignComponentDto[] responses = objectMapper.readValue(
          result.getResponse().getContentAsString(), DesignComponentDto[].class);

      assertThat(responses).hasSize(2);
      assertThat(responses)
          .extracting(DesignComponentDto::getDesignComponentId)
          .containsExactlyInAnyOrder(
              componentAOnly.getDesignComponentId(),
              componentAAndB.getDesignComponentId()
          );
      assertThat(responses).allSatisfy(response ->
          assertThat(response.getComponentTypes())
              .extracting("componentTypeId")
              .contains(componentTypeA.getComponentTypeId()));
    }

    @Test
    @DisplayName("존재하지 않는 componentTypeId 조회 시 404 반환")
    void getDesignComponentsByUnknownComponentTypeId() throws Exception {
      MockHttpServletRequestBuilder requestBuilder = get(
          "/api/design-components/component-types/{componentTypeId}", 999999);

      mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("componentType에 속한 DesignComponent가 없으면 빈 리스트 반환")
    void getDesignComponentsByComponentTypeId_emptyResult() throws Exception {
      // Given
      designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image-b.png", true, List.of(componentTypeB));

      // When
      MockHttpServletRequestBuilder requestBuilder = get(
          "/api/design-components/component-types/{componentTypeId}",
          componentTypeA.getComponentTypeId());

      MvcResult result = mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
          .andExpect(status().isOk())
          .andReturn();

      // Then
      DesignComponentDto[] responses = objectMapper.readValue(
          result.getResponse().getContentAsString(), DesignComponentDto[].class);
      assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("특정 사용자의 DesignComponent 목록 조회 테스트")
    void getDesignComponentsByPublicUserId() throws Exception {
      // Given
      User otherUser = userDataGenerator.generateTestUser("other@test.com");

      // testUser의 design components
      designComponentDataGenerator.generateDesignComponent(testUser,
          "http://example.com/image1.png", true, List.of(componentTypeA));
      designComponentDataGenerator.generateDesignComponent(testUser,
          "http://example.com/image2.png", true, List.of(componentTypeA));
      designComponentDataGenerator.generateDesignComponent(testUser,
          "http://example.com/image3.png", true, List.of(componentTypeB));

      // otherUser의 design component
      designComponentDataGenerator.generateDesignComponent(otherUser,
          "http://example.com/other1.png", true, List.of(componentTypeA));
      designComponentDataGenerator.generateDesignComponent(otherUser,
          "http://example.com/other2.png", true, List.of(componentTypeB));

      // When & Then - testUser의 desgin components만 조회
      MockHttpServletRequestBuilder requestBuilder = get(
          "/api/design-components/user/{publicUserId}",
          testUser.getPublicUserId());

      MvcResult result = mockMvc.perform(requestBuilder)
          .andExpect(status().isOk())
          .andReturn();

      String responseContent = result.getResponse().getContentAsString();
      DesignComponentDto[] components = objectMapper.readValue(responseContent,
          DesignComponentDto[].class);

      assertThat(components).hasSize(3);
      assertThat(components)
          .extracting(DesignComponentDto::getImageUrl)
          .containsExactlyInAnyOrder(
              "http://example.com/image1.png",
              "http://example.com/image2.png",
              "http://example.com/image3.png"
          );
      assertThat(components).allMatch(
          dto -> dto.getPublicUserId().equals(testUser.getPublicUserId()));
    }

    @Test
    @DisplayName("DesignComponent 수정 테스트")
    void updateDesignComponent() throws Exception {
      // Given
      DesignComponent savedComponent = designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image.png", false, List.of(componentTypeA));
      UpdateDesignComponentRequest updateRequest = UpdateDesignComponentRequest.builder()
          .isPublic(true)
          .componentTypeIds(List.of(componentTypeB.getComponentTypeId()))
          .build();
      MockMultipartFile requestPart = createRequestPart(updateRequest);
      MockMultipartFile image = createImagePart();

      // When & Then
      DesignComponentDto response = mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<DesignComponentDto>builder()
              .mockMvc(mockMvc)
              .path("/api/design-components/" + savedComponent.getDesignComponentId())
              .httpMethod(HttpMethod.PUT)
              .formDataList(List.of(requestPart, image))
              .clientDto(testClient)
              .statusCode(200)
              .responseType(new TypeReference<>() {
              })
              .build()
      );
      assertThat(response.getDesignComponentId()).isEqualTo(savedComponent.getDesignComponentId());
      assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
      assertThat(response.getIsPublic()).isTrue();
      assertThat(response.getImageUrl()).isNotBlank();
      assertThat(response.getImageUrl()).isNotEqualTo("http://example.com/image.png");
      assertThat(response.getImageUrl()).isEqualTo("https://s3.example.com/uploaded-image.png");
      assertThat(response.getCreatedAt()).isNotNull();
      assertThat(response.getUpdatedAt()).isNotNull();
      assertThat(response.getComponentTypes())
          .extracting("componentTypeId")
          .containsExactly(componentTypeB.getComponentTypeId());

      DesignComponent updated = designComponentRepository.findByDesignComponentId(
          savedComponent.getDesignComponentId()).orElseThrow();
      assertThat(updated.getComponentTypes())
          .extracting(ComponentType::getComponentTypeId)
          .containsExactly(componentTypeB.getComponentTypeId());
    }

    @Test
    @DisplayName("다른 사용자의 DesignComponent 수정 실패 테스트")
    void updateDesignComponentByOtherUser() throws Exception {
      // Given - 다른 사용자가 만든 컴포넌트
      User otherUser = userDataGenerator.generateTestUser("other@test.com");
      DesignComponent savedComponent = designComponentDataGenerator.generateDesignComponent(
          otherUser, "http://example.com/image.png", false, List.of(componentTypeA));
      UpdateDesignComponentRequest updateRequest = UpdateDesignComponentRequest.builder()
          .isPublic(true)
          .componentTypeIds(List.of(componentTypeB.getComponentTypeId()))
          .build();
      MockMultipartFile requestPart = createRequestPart(updateRequest);
      MockMultipartFile image = createImagePart();
      // When & Then - testUser로 수정 시도 (실패해야 함)
      mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<Void>builder()
              .mockMvc(mockMvc)
              .path("/api/design-components/" + savedComponent.getDesignComponentId())
              .httpMethod(HttpMethod.PUT)
              .formDataList(List.of(requestPart, image))
              .clientDto(testClient)
              .statusCode(403)
              .responseType(new TypeReference<>() {
              })
              .build()
      );

      // 수정 안됐는지 확인
      DesignComponent afterComponent = designComponentRepository.findById(
          savedComponent.getDesignComponentId()).get();
      assertThat(afterComponent.getImageUrl()).isEqualTo("http://example.com/image.png");
      assertThat(afterComponent.getIsPublic()).isFalse();
      assertThat(afterComponent.getComponentTypes())
          .extracting(ComponentType::getComponentTypeId)
          .containsExactly(componentTypeA.getComponentTypeId());

    }

    @Test
    @DisplayName("DesignComponent 생성 시 componentTypeIds 누락 검증")
    void createDesignComponent_withoutComponentTypeIds() throws Exception {
      CreateDesignComponentRequest createRequest = CreateDesignComponentRequest.builder()
          .isPublic(true)
          .build();
      MockMultipartFile requestPart = createRequestPart(createRequest);
      MockMultipartFile image = createImagePart();
      mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<Void>builder()
              .mockMvc(mockMvc)
              .path("/api/design-components")
              .httpMethod(HttpMethod.POST)
              .formDataList(List.of(requestPart, image))
              .clientDto(testClient)
              .statusCode(400)
              .responseType(new TypeReference<>() {
              })
              .build()
      );

      verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
    }

    @Test
    @DisplayName("DesignComponent 생성 시 중복 componentTypeIds 검증")
    void createDesignComponent_duplicateComponentTypeIds() throws Exception {
      CreateDesignComponentRequest createRequest = CreateDesignComponentRequest.builder()
          .isPublic(true)
          .componentTypeIds(
              List.of(componentTypeA.getComponentTypeId(), componentTypeA.getComponentTypeId()))
          .build();
      MockMultipartFile requestPart = createRequestPart(createRequest);
      MockMultipartFile image = createImagePart();
      mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<Void>builder()
              .mockMvc(mockMvc)
              .path("/api/design-components")
              .httpMethod(HttpMethod.POST)
              .formDataList(List.of(requestPart, image))
              .clientDto(testClient)
              .statusCode(400)
              .responseType(new TypeReference<>() {
              })
              .build()
      );
    }

    @Test
    @DisplayName("DesignComponent 생성 시 존재하지 않는 componentTypeId 검증")
    void createDesignComponent_withUnknownComponentTypeId() throws Exception {
      CreateDesignComponentRequest createRequest = CreateDesignComponentRequest.builder()
          .isPublic(true)
          .componentTypeIds(List.of(componentTypeA.getComponentTypeId(), 999999))
          .build();
      MockMultipartFile requestPart = createRequestPart(createRequest);
      MockMultipartFile image = createImagePart();
      mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<Void>builder()
              .mockMvc(mockMvc)
              .path("/api/design-components")
              .httpMethod(HttpMethod.POST)
              .formDataList(List.of(requestPart, image))
              .clientDto(testClient)
              .statusCode(404)
              .responseType(new TypeReference<>() {
              })
              .build()
      );

      verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
    }

    @Test
    @DisplayName("DesignComponent 수정 시 존재하지 않는 componentTypeId 검증")
    void updateDesignComponent_withUnknownComponentTypeId() throws Exception {
      DesignComponent savedComponent = designComponentDataGenerator.generateDesignComponent(
          testUser, "http://example.com/image.png", false, List.of(componentTypeA));
      UpdateDesignComponentRequest updateRequest = UpdateDesignComponentRequest.builder()
          .isPublic(true)
          .componentTypeIds(List.of(999999))
          .build();
      MockMultipartFile requestPart = createRequestPart(updateRequest);
      MockMultipartFile image = createImagePart();
      mockMvcUtils.doAuthMultipartRequest(
          MockMvcMultipartRequestDto.<Void>builder()
              .mockMvc(mockMvc)
              .path("/api/design-components/" + savedComponent.getDesignComponentId())
              .httpMethod(HttpMethod.PUT)
              .formDataList(List.of(requestPart, image))
              .clientDto(testClient)
              .statusCode(404)
              .responseType(new TypeReference<>() {
              })
              .build()
      );

      verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
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
          .generateDesignComponent(otherUser, "https://other.com/image.png", false,
              List.of(componentTypeA));

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


}
