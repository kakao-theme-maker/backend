package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.security.UserRole;
import com.komentum.global.utils.FileManager;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.data.DesignComponentDataGenerator;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
  private MockMvcUtils mockMvcUtils;

  @MockitoBean
  private FileManager fileManager;

  @Autowired
  private DesignComponentDataGenerator designComponentDataGenerator;

  @Autowired
  private UserDataGenerator userDataGenerator;

  private User testUser;
  private TestClientDto testClient;


  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
    testUser = userDataGenerator.generateTestUser("test@example.com");
    testClient = TestClientDto.fromEntity(testUser);

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
    userDataGenerator.deleteAllUsers();
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


}
