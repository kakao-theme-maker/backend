package com.komentum.user.controller;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.JwtUtils;
import com.komentum.global.dto.CustomResponse;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.dto.UserUpdateDto;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
public class UserRetrieveControllerTest {

  String email = "admin1@gmail.com";
  String password = "qwer123!";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserDataGenerator userDataGenerator;
  @Autowired
  private ObjectMapper objectMapper;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private JwtUtils jwtUtils;

  User user;
  @Autowired
  private MockMvcUtils mockMvcUtils;

  @BeforeEach
  void setUp() {
    userDataGenerator.deleteAllUsers();
    userDataGenerator.generateRetrieveTestUser(email, password);
    addPostForUser(email);
    user = userRepository.findByUserEmail(email).orElseThrow();
  }


  @AfterEach
  void tearDown() {
    userDataGenerator.deleteAllUsers();
  }

  // upload 확인용
  private void addPostForUser(String email) {
    User user = userRepository.findByUserEmail(email)
        .orElseThrow();
    Post post = Post.builder()
        .title("test")
        .content("test-content")
        .user(user)
        .build();

    postRepository.save(post);
  }

  @Test
  @DisplayName("유저 조회")
  void inquiryUserTest() throws Exception {
    //given
    UserResponseDto userResponseDto =
        UserResponseDto.builder()
            .userEmail(user.getUserEmail())
            .name(user.getName())
            .profileImage(user.getProfileImg())
            .publicUserId(user.getPublicUserId())
            .uploads(1)
            .followers(0)
            .following(0)
            .build();

    String token = jwtUtils.generateAccessToken(user.getPublicUserId());

    //when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/users")
        .param("userPublicID", user.getPublicUserId())
        .header("Authorization", "Bearer " + token);

    //then
    String response = mockMvc.perform(request)
        .andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    //UserInquiryResponseDto는 래핑된 값
    CustomResponse<UserResponseDto> wrapper =
        objectMapper.readValue(response, new TypeReference<>() {
        });

    UserResponseDto result = wrapper.getData();

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(userResponseDto);
  }

  @Test
  @DisplayName("현재 인증된 사용자 정보 조회")
  void retrieveCurrentUser_success() throws Exception {
    // given
    User targetUser = userDataGenerator.generateTestUser(UUID.randomUUID() + "@test.com");
    // when
    UserResponseDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, UserResponseDto>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(targetUser))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getUserEmail()).isEqualTo(targetUser.getUserEmail());
    assertThat(response.getName()).isEqualTo(targetUser.getName());
    assertThat(response.getPublicUserId()).isEqualTo(targetUser.getPublicUserId());
  }

  @Test
  @DisplayName("유저 정보 수정")
  void updateUserTest() throws Exception {
    // given
    String updatedUserName = "updatedName";
    String updatedUserProfileUrl = "https://updatedUrl";
    Gender updatedGender = Gender.male;
    LocalDate updatedBirth = LocalDate.of(2000, 1, 1);

    UserUpdateDto updateDto = UserUpdateDto.builder()
        .name(updatedUserName)
        .profileImage(updatedUserProfileUrl)
        .gender(updatedGender)
        .birth(updatedBirth)
        .build();
    String token = jwtUtils.generateAccessToken(user.getPublicUserId());

    //when
    MockHttpServletRequestBuilder request =
        MockMvcRequestBuilders.patch("/api/users/me")
            .content(objectMapper.writeValueAsString(updateDto))
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token);

    String response = mockMvc.perform(request)
        .andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();
    //then
    CustomResponse<UserResponseDto> wrapper =
        objectMapper.readValue(response, new TypeReference<>() {
        });

    UserResponseDto result = wrapper.getData();

    // 응답 검증
    assertThat(result.getName()).isEqualTo(updatedUserName);
    assertThat(result.getProfileImage()).isEqualTo(updatedUserProfileUrl);

    // DB 검증
    User updatedUser = userRepository.findByUserEmail(email).orElseThrow();
    assertThat(updatedUser.getName()).isEqualTo(updatedUserName);
    assertThat(updatedUser.getProfileImg()).isEqualTo(updatedUserProfileUrl);
    assertThat(updatedUser.getGender()).isEqualTo(updatedGender);
    assertThat(updatedUser.getBirth()).isEqualTo(updatedBirth);
  }
}
