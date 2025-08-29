package com.komentum.post.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import com.komentum.auth.JwtUtils;
import com.komentum.config.EnableTestProfile;
import com.komentum.config.PostTestDataGenerator;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostDto.PostCreateDto;
import com.komentum.post.dto.PostDto.PostResponse;
import com.komentum.post.dto.PostDto.PostUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.user.domain.User;
import com.komentum.utils.MockMvcUtils;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class PostControllerTest {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private PostTestDataGenerator postTestDataGenerator;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private JwtUtils jwtUtils;

  private String jwtToken;

  @BeforeEach
  void setUp() {
    postTestDataGenerator.deleteData();
    postTestDataGenerator.generateData(1, 10, 1);
    jwtToken = jwtUtils.generateAccessToken(postTestDataGenerator.users.get(0).getUserEmail());
  }

  @AfterEach
  void tearDown() {
    postTestDataGenerator.deleteData();
  }

  @Test
  @DisplayName("success test of get posts by page")
  void getPosts_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 5;
    int pageSizeOverflow = 100;
    // when - get 5 of 10
    MockHttpServletRequestBuilder requestBuilderExpected5 = MockMvcRequestBuilders.get("/posts")
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSize));
    // when - get all by big size page
    MockHttpServletRequestBuilder requestBuilderExpected10 = MockMvcRequestBuilders.get("/posts")
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSizeOverflow));
    // then - get 5 of 10
    List<PostResponse> postResponsesExpected5 = mockMvcUtils.performAuthRequestForList(mockMvc,
        requestBuilderExpected5, jwtToken);
    assertEquals(pageSize, postResponsesExpected5.size());
    // then - get all by big size page
    List<PostResponse> postResponsesExpected10 = mockMvcUtils.performAuthRequestForList(mockMvc,
        requestBuilderExpected10, jwtToken);
    assertEquals(postResponsesExpected10.size(), postRepository.count());
  }

  @Test
  @DisplayName("success test of create post")
  void createPost_success() throws Exception {
    // given
    Faker faker = new Faker();
    User user = postTestDataGenerator.users.get(0);
    PostCreateDto postCreateDto = PostCreateDto.builder()
        .title(faker.lorem().sentence())
        .content(faker.lorem().paragraph())
        .userEmail(user.getUserEmail())
        .build();
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/posts")
        .content(objectMapper.writeValueAsString(postCreateDto));
    // then
    PostResponse postResponse = mockMvcUtils.performAuthRequest(mockMvc, requestBuilder, jwtToken,
        PostResponse.class);
    assert (postRepository.findById(postResponse.getPostId()).isPresent());
    assertEquals(postCreateDto.getTitle(), postResponse.getTitle());
  }

  @Test
  @DisplayName("success test of update post")
  void updatePost() throws Exception {
    // given
    Post toUpdate = postTestDataGenerator.posts.get(0);
    String updateValue = UUID.randomUUID().toString();
    PostUpdateDto postUpdateDto = PostUpdateDto.builder()
        .title(updateValue)
        .content(null)
        .build();
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put("/posts/{postId}",
            toUpdate.getPostId())
        .content(objectMapper.writeValueAsString(postUpdateDto));
    // then
    PostResponse postResponse = mockMvcUtils.performAuthRequest(mockMvc, requestBuilder, jwtToken,
        PostResponse.class);
    assertEquals(updateValue, postResponse.getTitle());
    assertEquals(toUpdate.getContent(), postResponse.getContent());
  }

  @Test
  @DisplayName("success test of delete post")
  void deletePost() throws Exception {
    // given
    Post toDelete = postTestDataGenerator.posts.get(0);
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete("/posts/{postId}",
        toDelete.getPostId());
    // then
    mockMvcUtils.performAuthRequest(mockMvc, requestBuilder, jwtToken);
    assert (postRepository.findById(toDelete.getPostId()).isEmpty());
    assertEquals(postRepository.count(), postTestDataGenerator.posts.size() - 1);
  }
}