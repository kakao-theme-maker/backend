package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.JwtUtils;
import com.komentum.config.EnableTestProfile;
import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.S3FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.UserDataGenerator;
import com.komentum.user.domain.User;
import com.komentum.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
public class UserPostControllerTest {
  private final String userEmail = "admin@gmail.com";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserDataGenerator userDataGenerator;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private PostRepository postRepository;
  @Autowired
  private JwtUtils jwtUtils;

  // fileManger.resolveFilePath 미작동으로 인해
  @MockitoBean
  FileManager fileManager;

  @Autowired
  private ObjectMapper objectMapper;

  // NoSuchBeanDefinitionException: No qualifying bean of type 'S3FileManager' 에러로 인해
   @MockitoBean
   S3FileManager s3FileManager;

  @BeforeEach
  void setUp(){
    userDataGenerator.deleteAllUsers();
    userDataGenerator.generateTestUser(userEmail);
    addPost(userEmail);
  }

  @AfterEach
  void tearDown(){
    userDataGenerator.deleteAllUsers();
  }

  void addPost(String email){
    User user = userRepository.findByUserEmail(email).orElseThrow();
    for (int i = 1; i <= 6; i++){
      Post post = Post.builder()
          .previewImageName("image-" + i)
          .user(user)
          .build();
      postRepository.save(post);
    }
  }

  @Test
  @DisplayName("유저가 작성한 게시글 목록 조회")
  void getUserPostTest() throws Exception{
    //given
    // resolveFilePath가 일단 mocked 객체 리턴
    given(fileManager.resolveFilePath(any()))
        .willReturn("http://mocked-url");

    String token = jwtUtils.generateAccessToken(userEmail);

    //when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get("/api/users/me/upload-posts")
        .header("Authorization", "Bearer " + token);

    //then
    String response = mockMvc.perform(request)
        .andExpect(status().is2xxSuccessful())
        .andReturn().getResponse().getContentAsString();

    List<UserPostListResponseDto> result = objectMapper.readValue(response,
        new TypeReference<>() { });

    // 유저 게시글 목록 전부 반환하는 지 확인
    assertThat(result).hasSize(6);

    UserPostListResponseDto firstPost = result.get(0);

    // postId, 생성&수정 시간, previewImageUrl 반환하는 지 확인
    assertThat(firstPost.getPostId()).isNotNull();
    assertThat(firstPost.getCreatedAt()).isNotNull();
    assertThat(firstPost.getUpdatedAt()).isNotNull();
    assertThat(firstPost.getPreviewImageUrl()).isNotBlank();
  }
}