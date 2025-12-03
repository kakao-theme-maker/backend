package com.komentum.user.controller;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.config.EnableTestProfile;
import com.komentum.config.RedisEmbeddedConfig;
import com.komentum.global.dto.UserInquiryResponseDto;
import com.komentum.global.security.UserRole;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.DesignBoardDto.DesignBoardCreateDto;
import com.komentum.post.repository.DesignBoardRepository;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.UserDataGenerator;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.user.domain.Gender;
import com.komentum.user.domain.User;
import com.komentum.user.dto.LocalLoginRequestDto;
import com.komentum.user.dto.UserAuthResponse;
import com.komentum.user.dto.UserResponseDto;
import com.komentum.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import com.fasterxml.jackson.core.type.TypeReference;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
// @Import(RedisEmbeddedConfig.class)
public class UserRetrieveControllerTest {

  String email = "admin1@gmail.com";
  String password ="qwer123!";

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

  @BeforeEach
  void setUp(){
    userDataGenerator.deleteAllUsers();
    userDataGenerator.generateRetrieveTestUser(email, password);
    addPostForUser(email);
  }

  @AfterEach
  void tearDown(){
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

  // 정보를 조회하기 위해서 token이 필요함
  private String checkToken() throws Exception {
    //jwt 발급
    LocalLoginRequestDto loginRequestDto = LocalLoginRequestDto.builder()
        .email(email)
        .password(password)
        .build();

    String token = objectMapper.readValue(
        mockMvc.perform(
                MockMvcRequestBuilders.post("/api/auth/local/sign-in")
                    .content(objectMapper.writeValueAsString(loginRequestDto))
                    .contentType(MediaType.APPLICATION_JSON)
            ).andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString(), UserAuthResponse.class).getAccessToken();

    return token;

  }

  @Test
  @DisplayName("유저 조회")
  void inquiryUserTest() throws Exception{
    //given
    String userEmail = "admin1@gmail.com";
    String userName = "admin";
    String userProfileUrl = "https://example";
    int uploads = 1;
    int followers = 0;
    int following = 0;
   // createdAt은 생성된 시간으로 나오기 때문에, Test 불가

    UserResponseDto userResponseDto =
        UserResponseDto.builder()
            .userEmail(userEmail)
            .userName(userName)
            .userProfileUrl(userProfileUrl)
            .uploads(uploads)
            .followers(followers)
            .following(following)
            .build();

    String token = checkToken();

    //when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/users/" + userEmail)
        .header("Authorization","Bearer " + token);

    //then
    String response = mockMvc.perform(request)
        .andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    //UserInquiryResponseDto가 래핑된 값
    UserInquiryResponseDto<UserResponseDto> wrapper =
        objectMapper.readValue(response, new TypeReference<>() {});

    UserResponseDto result = wrapper.getData();

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(userResponseDto);
  }
}
