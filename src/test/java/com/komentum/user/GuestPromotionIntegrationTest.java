package com.komentum.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.auth.JwtUtils;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.global.properties.AuthProperty;
import com.komentum.global.security.UserRole;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto.ExecutionContext;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.repository.ThemeComponentRepository;
import com.komentum.user.domain.User;
import com.komentum.user.dto.SignUpRequestDto;
import com.komentum.user.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * 게스트 사용자가 리소스를 생성한 뒤 회원가입(로컬/카카오)으로 승격되어도, 사용자 ID와 리소스 소유권이 유지되는지 검증한다.
 */
@SpringBootTest(properties = "spring.jpa.open-in-view=false")
@EnableTestProfile
@AutoConfigureMockMvc
class GuestPromotionIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private MockMvcUtils mockMvcUtils;
  @Autowired
  private TestDataRemover testDataRemover;
  @Autowired
  private UserScenarioSupport userScenarioSupport;
  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;
  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;
  @Autowired
  private ThemeComponentRepository themeComponentRepository;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private JwtUtils jwtUtils;
  @Autowired
  private TransactionTemplate transactionTemplate;

  private static final String SIGN_UP_EMAIL = "promoted-guest@test.com";

  @BeforeEach
  void setUp() {
    // 테마 생성 API가 복사할 디폴트 테마 준비
    List<User> seedUsers = userScenarioSupport.builder().withUsers(1).build().users();
    List<DesignComponent> designComponents = designComponentScenarioSupport.builder(seedUsers)
        .withCountPerUser(5)
        .build().designComponents();
    themeComponentScenarioSupport.builder(seedUsers, designComponents)
        .withCountPerUser(1)
        .withDefaultTheme()
        .build();
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  /**
   * 게스트 토큰 발급 API를 호출하고, 응답 쿠키의 게스트 토큰을 반환한다
   */
  private Cookie issueGuestToken() throws Exception {
    ResultActions result = mockMvcUtils.performRequest(
        MockMvcRequestBuilders.post("/api/guest-tokens"),
        ExecutionContext.builder().mockMvc(mockMvc).build());
    result.andExpect(status().isOk());
    Cookie guestCookie = result.andReturn().getResponse()
        .getCookie(AuthProperty.ACCESS_TOKEN_COOKIE_NAME);
    assertThat(guestCookie).isNotNull();
    return guestCookie;
  }

  /**
   * 게스트 토큰으로 테마 생성 API를 호출하고, 생성된 테마 ID를 반환한다
   */
  private Integer createThemeWithGuestToken(Cookie guestCookie) throws Exception {
    ResultActions result = mockMvcUtils.performRequest(
        MockMvcRequestBuilders.post("/api/themes").cookie(guestCookie),
        ExecutionContext.builder().mockMvc(mockMvc).build());
    result.andExpect(status().isCreated());
    ThemeDetailResponse response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    return response.getThemeComponentId();
  }

  /**
   * 테마 소유자의 userId를 조회한다
   */
  private Long findThemeOwnerId(Integer themeComponentId) {
    return transactionTemplate.execute(status ->
        themeComponentRepository.findById(themeComponentId).orElseThrow().getUser().getUserId());
  }

  /**
   * 이메일이 같은 사용자 수를 조회한다
   */
  private long countUsersByEmail(String email) {
    return userRepository.findAll().stream()
        .filter(user -> email.equals(user.getUserEmail()))
        .count();
  }

  @Test
  @DisplayName("게스트가 테마를 생성한 뒤 게스트 토큰을 유지한 채 로컬 회원가입하면, 동일한 ID로 USER 승격되고 테마 소유권이 유지된다")
  void localSignUp_afterCreatingTheme_keepUserIdAndOwnership() throws Exception {
    // given: 게스트 토큰 발급 및 게스트로 테마 생성
    Cookie guestCookie = issueGuestToken();
    String guestPublicUserId = jwtUtils.getUserId(guestCookie.getValue());
    User guest = userRepository.findByPublicUserId(guestPublicUserId).orElseThrow();
    assertThat(guest.getRole()).isEqualTo(UserRole.GUEST);
    Integer themeId = createThemeWithGuestToken(guestCookie);
    long userCount = userRepository.count();
    // when: 게스트 토큰을 유지한 채 로컬 회원가입
    mockMvcUtils.performRequest(
            MockMvcRequestBuilders.post("/api/auth/local/sign-up").cookie(guestCookie),
            ExecutionContext.builder()
                .mockMvc(mockMvc)
                .body(new SignUpRequestDto(SIGN_UP_EMAIL, "qwer123!"))
                .build())
        .andExpect(status().isOk());
    // then: 사용자 ID 유지 및 권한 변경
    User promoted = userRepository.findByUserEmail(SIGN_UP_EMAIL).orElseThrow();
    assertThat(promoted.getUserId()).isEqualTo(guest.getUserId());
    assertThat(promoted.getPublicUserId()).isEqualTo(guestPublicUserId);
    assertThat(promoted.getRole()).isEqualTo(UserRole.USER);
    // then: 테마 리소스 존재 및 소유권 유지
    assertThat(themeComponentRepository.findById(themeId)).isPresent();
    assertThat(findThemeOwnerId(themeId)).isEqualTo(guest.getUserId());
    // then: 사용자 중복 생성 없음
    assertThat(userRepository.count()).isEqualTo(userCount);
    assertThat(countUsersByEmail(SIGN_UP_EMAIL)).isEqualTo(1);
    // then: 같은 이메일로 다시 가입하면 중복 생성되지 않는다
    mockMvcUtils.performRequest(
            MockMvcRequestBuilders.post("/api/auth/local/sign-up"),
            ExecutionContext.builder()
                .mockMvc(mockMvc)
                .body(new SignUpRequestDto(SIGN_UP_EMAIL, "qwer123!"))
                .build())
        .andExpect(status().isConflict());
    assertThat(countUsersByEmail(SIGN_UP_EMAIL)).isEqualTo(1);
  }
}
