package com.komentum.theme.controller;

import static com.komentum.test.fixture.component.DesignComponentRequestFixture.UPLOADED_IMAGE_URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.enums.TypeCode;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
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
  protected ComponentTypeRepository componentTypeRepository;

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
  protected ComponentType componentTypeA;
  protected ComponentType componentTypeB;
  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUpDesignComponentControllerTest() {
//    designComponentRepository.deleteAll();
//    componentTypeRepository.deleteAll();
//    userDataGenerator.deleteAllUsers();
//    reset(fileManager);

    testUser = userDataGenerator.generateTestUser("test@example.com");
    testClient = TestClientDto.fromEntity(testUser);
    componentTypeA = createComponentType("comp-a");
    componentTypeB = createComponentType("comp-b");
    componentTypeRepository.flush();
    userRepository.flush();

    authenticateAs(testUser);
  }

  @AfterEach
  void tearDownDesignComponentControllerTest() {
    //    designComponentDataGenerator.deleteDesignComponents();
    //    componentTypeRepository.deleteAll();
    //    userDataGenerator.deleteAllUsers();
    SecurityContextHolder.clearContext();
    reset(fileManager);
  }

  protected void stubImageUpload() {
    when(fileManager.uploadFile(any(byte[].class), anyString()))
        .thenReturn(UPLOADED_IMAGE_URL);
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

  protected DesignComponent testUserComponent(String imageUrl, boolean isPublic,
      ComponentType... componentTypes) {
    return componentForUser(testUser, imageUrl, isPublic, componentTypes);
  }

  protected DesignComponent testUserComponent() {
    return designComponentDataGenerator.generateDesignComponent(testUser);
  }

  protected DesignComponent otherUserComponent(String email, String imageUrl, boolean isPublic,
      ComponentType... componentTypes) {
    return componentForUser(createOtherUser(email), imageUrl, isPublic, componentTypes);
  }

  protected User createOtherUser(String email) {
    return userDataGenerator.generateTestUser(email);
  }

  private DesignComponent componentForUser(User user, String imageUrl, boolean isPublic,
      ComponentType... componentTypes) {
    return designComponentDataGenerator.generateDesignComponent(
        user, imageUrl, isPublic, List.of(componentTypes));
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

  private ComponentType createComponentType(String suffix) {
    return componentTypeRepository.save(ComponentType.builder()
        .typeCode(typeCodeFor(suffix))
        .name("test component type " + suffix)
        .explain("test component type explain " + suffix)
        .build());
  }

  private TypeCode typeCodeFor(String suffix) {
    return switch (suffix) {
      case "comp-a" -> TypeCode.MAIN_TAB_BG_IMAGE;
      case "comp-b" -> TypeCode.MAIN_TAB_ICO_FRIENDS;
      default -> TypeCode.COMMON_ICO_THEME;
    };
  }
}
