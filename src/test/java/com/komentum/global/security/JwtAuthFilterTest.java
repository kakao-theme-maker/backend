package com.komentum.global.security;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.github.javafaker.Faker;
import com.komentum.auth.JwtUtils;
import com.komentum.controller.TestController;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class JwtAuthFilterTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SecurityProperties securityProperties;

  private User user;

  @BeforeEach
  void setUp() {
    Faker faker = new Faker();
    User newUser = User.builder()
        .publicUserId(UUID.randomUUID().toString())
        .userEmail(faker.internet().emailAddress())
        .role(UserRole.USER)
        .birth(LocalDate.now().minusYears(10))
        .gender(Gender.male)
        .profileImg(faker.internet().image())
        .introduce(faker.lorem().word())
        .build();
    user = userRepository.save(newUser);
  }

  @AfterEach
  void tearDown() {
    userRepository.deleteAll();
  }

  private String getUserAccessToken() {
    return jwtUtils.generateAccessToken(user.getPublicUserId());
  }

  @Test
  @DisplayName("success test about permit-all path")
  void jwtAuthFilter_permitAll_success() throws Exception {
    // given
    String whiteListPath = securityProperties.getWhiteList()[0];
    // when and then
    RequestBuilder builder = MockMvcRequestBuilders.get(whiteListPath);
    mockMvc.perform(builder).andExpect(status().isOk()).andDo(print());
  }

  @Test
  @DisplayName("success test about permit-get path")
  void jwtAuthFilter_permitGet_success() throws Exception {
    // given
    String whiteListPath = securityProperties.getWhiteListGet()[0];
    // when
    RequestBuilder builder = MockMvcRequestBuilders.get(whiteListPath);
    mockMvc.perform(builder).andExpect(status().isOk()).andDo(print());
  }

  @Test
  @DisplayName("success test when token is valid and path requires token")
  void jwtAuthFilter_whenTokenIsValid_success() throws Exception {
    // given
    String token = getUserAccessToken();
    // when
    RequestBuilder builder = MockMvcRequestBuilders.get(TestController.authRequiredPath)
        .header("Authorization", "Bearer " + token);
    mockMvc.perform(builder).andExpect(status().isOk()).andDo(print());
  }

  @Test
  @DisplayName("error test when token doesn't exist but path requires token")
  void jwtAuthFilter_4xx_noToken() throws Exception {
    // when and then
    RequestBuilder builder = MockMvcRequestBuilders.get(TestController.authRequiredPath);
    mockMvc.perform(builder).andExpect(status().is4xxClientError()).andDo(print());
  }

  @Test
  @DisplayName("error test when token isn't valid but path requires token")
  void jwtAuthFilter_4xx_invalidToken() throws Exception {
    // given
    String token = UUID.randomUUID().toString();
    // when and then
    RequestBuilder builder = MockMvcRequestBuilders.get(TestController.authRequiredPath)
        .header("Authorization", "Bearer " + token);
    mockMvc.perform(builder).andExpect(status().is4xxClientError()).andDo(print());
  }
}