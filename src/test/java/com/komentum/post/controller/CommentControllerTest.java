package com.komentum.post.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.auth.JwtUtils;
import com.komentum.config.EnableTestProfile;
import com.komentum.config.PostTestDataGenerator;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentResponse;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.dto.PostDto.ThemeBoardDetailDto;
import com.komentum.post.repository.CommentRepository;
import com.komentum.user.domain.User;
import com.komentum.utils.MockMvcUtils;
import java.util.List;
import java.util.UUID;
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
class CommentControllerTest {

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private JwtUtils jwtUtils;

  @Autowired
  private PostTestDataGenerator postTestDataGenerator;

  private String jwtToken;

  @BeforeEach
  void setUp() {
    postTestDataGenerator.deleteData();
    postTestDataGenerator.generateData(1, 1, 10);
    jwtToken = jwtUtils.generateAccessToken(postTestDataGenerator.users.get(0).getUserEmail());
  }

  @Test
  @DisplayName("success test of get comments by page")
  void getComments_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 5;
    int pageSizeOverflow = 100;
    Post post = postTestDataGenerator.posts.get(0);
    // when - get 5 of 10
    MockHttpServletRequestBuilder requestBuilderExpected5 = MockMvcRequestBuilders.get(
            "/posts/{postId}/comments", post.getPostId())
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSize));
    // when - get all by big size page
    MockHttpServletRequestBuilder requestBuilderExpected10 = MockMvcRequestBuilders.get(
            "/posts/{postId}/comments", post.getPostId())
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSizeOverflow));
    // then - get 5 of 10
    List<CommentResponse> expected5 = mockMvcUtils.performAuthRequestForList(mockMvc,
        requestBuilderExpected5, jwtToken);
    assertEquals(pageSize, expected5.size());
    // then - get all by big size page
    List<ThemeBoardDetailDto> expected10 = mockMvcUtils.performAuthRequestForList(mockMvc,
        requestBuilderExpected10, jwtToken);
    assertEquals(expected10.size(), commentRepository.count());
  }

  @Test
  @DisplayName("success test of get comment by id")
  void getComment() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
        "/posts/comments/{commentId}", target.getCommentId());
    // then
    CommentResponse commentResponse = mockMvcUtils.performAuthRequest(mockMvc, requestBuilder,
        jwtToken, CommentResponse.class);
    assertEquals(target.getCommentId(), commentResponse.getCommentId());
  }

  @Test
  @DisplayName("success test of create new comment")
  void createComment() throws Exception {
    // given
    String content = UUID.randomUUID().toString();
    User user = postTestDataGenerator.users.get(0);
    Post post = postTestDataGenerator.posts.get(0);
    CommentCreateDto commentCreateDto = CommentCreateDto.builder()
        .content(content)
        .postId(post.getPostId())
        .userEmail(user.getUserEmail())
        .build();
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/posts/comments")
        .content(objectMapper.writeValueAsString(commentCreateDto));
    // then
    CommentResponse commentResponse = mockMvcUtils.performAuthRequest(mockMvc, requestBuilder,
        jwtToken, CommentResponse.class);
    assert commentRepository.findById(commentResponse.getCommentId()).isPresent();
  }

  @Test
  @DisplayName("success test of update comment")
  void updateComment() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    CommentUpdateDto updateDto = CommentUpdateDto.builder()
        .content(UUID.randomUUID().toString())
        .build();
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put(
            "/posts/comments/{commentId}", target.getCommentId())
        .content(objectMapper.writeValueAsString(updateDto));
    // then
    CommentResponse response = mockMvcUtils.performAuthRequest(mockMvc, requestBuilder, jwtToken,
        CommentResponse.class);
    assertEquals(target.getCommentId(), response.getCommentId());
    assertEquals(updateDto.getContent(), response.getContent());
  }

  @Test
  @DisplayName("success test of delete comment")
  void deleteComment() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
        "/posts/comments/{commentId}", target.getCommentId());
    // then
    mockMvcUtils.performAuthRequest(mockMvc, requestBuilder, jwtToken);
    assert commentRepository.findById(target.getCommentId()).isEmpty();
  }
}