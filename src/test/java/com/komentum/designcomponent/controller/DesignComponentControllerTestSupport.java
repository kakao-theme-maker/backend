package com.komentum.designcomponent.controller;

import static com.komentum.test.fixture.component.DesignComponentRequestFixture.imageUrlOf;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.global.dto.CustomUserDetails;
import com.komentum.global.security.UserRole;
import com.komentum.global.utils.FileManager;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.fixture.component.DesignComponentDataGenerator;
import com.komentum.test.fixture.component.DesignComponentMultipartFixture;
import com.komentum.user.domain.User;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
abstract class DesignComponentControllerTestSupport {

  @Autowired
  protected MockMvc mockMvc;

  @Autowired
  protected ObjectMapper objectMapper;

  @Autowired
  protected DesignComponentRepository designComponentRepository;

  @Autowired
  protected MockMvcUtils mockMvcUtils;

  @MockitoBean
  protected FileManager fileManager;

  @Autowired
  protected DesignComponentDataGenerator designComponentDataGenerator;

  @Autowired
  protected UserDataGenerator userDataGenerator;

  @Autowired
  protected DesignComponentMultipartFixture multipartFixture;

  @Autowired
  protected PostScenarioSupport postScenarioSupport;

  @Autowired
  protected DesignComponentScenarioSupport designComponentScenarioSupport;

  @Autowired
  protected CategoryPostRepository categoryPostRepository;

  protected User testUser;
  protected TestClientDto testClient;

  @BeforeEach
  void setUpDesignComponentControllerTest() {
    testUser = userDataGenerator.generateTestUser("test@example.com");
    testClient = TestClientDto.fromEntity(testUser);

    when(fileManager.resolvePublicFileUrl(anyString()))
        .thenAnswer(invocation -> imageUrlOf(invocation.getArgument(0, String.class)));

    authenticateAs(testUser);
  }

  @AfterEach
  void tearDownDesignComponentControllerTest() {
    SecurityContextHolder.clearContext();
    reset(fileManager);
  }

  protected <R> R doMultipartRequest(String path, HttpMethod httpMethod, int statusCode,
      TypeReference<R> responseType, MockMultipartFile... formData) throws Exception {
    return mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<R>builder()
            .mockMvc(mockMvc)
            .path(path)
            .httpMethod(httpMethod)
            .formDataList(List.of(formData))
            .clientDto(testClient)
            .statusCode(statusCode)
            .responseType(responseType)
            .build()
    );
  }

  protected MvcResult performAuthenticated(MockHttpServletRequestBuilder requestBuilder,
      ResultMatcher statusMatcher) throws Exception {
    return mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
        .andExpect(statusMatcher)
        .andReturn();
  }

  /**
   * 현재 테스트 사용자가 소유하는 디자인 에셋을 생성한다.
   */
  protected DesignComponent testUserComponent(String fileName, boolean isPublic) {
    return designComponentDataGenerator.generateDesignComponent(testUser, fileName, isPublic);
  }

  protected DesignComponent testUserComponent() {
    return designComponentDataGenerator.generateDesignComponent(testUser);
  }

  /**
   * 다른 사용자가 소유하는 디자인 에셋을 생성한다.
   */
  protected DesignComponent otherUserComponent(String email, String fileName, boolean isPublic) {
    return designComponentDataGenerator.generateDesignComponent(
        createOtherUser(email), fileName, isPublic);
  }

  protected User createOtherUser(String email) {
    return userDataGenerator.generateTestUser(email);
  }

  private void authenticateAs(User user) {
    CustomUserDetails userDetails = CustomUserDetails.builder()
        .userEmail(user.getUserEmail())
        .publicUserId(user.getPublicUserId())
        .userRole(UserRole.USER)
        .build();

    Authentication auth = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(auth);
  }

}
