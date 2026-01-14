package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.config.EnableTestProfile;
import com.komentum.config.PostTestDataGenerator;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentResponse;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.repository.CommentRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
  private PostTestDataGenerator postTestDataGenerator;

  @BeforeEach
  void setUp() {
    postTestDataGenerator.deleteData();
    postTestDataGenerator.generateData(1, 1, 10);
  }

  @Test
  @DisplayName("success test of get comments by page")
  void getComments_success() throws Exception {
    // given
    Post post = postTestDataGenerator.posts.get(0);
    User author = postTestDataGenerator.users.get(0);
    String requestPath = String.format("/api/posts/%d/comments", post.getPostId());
    int pageNumber = 0;
    int pageSize = 5;
    MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
    queryParams.add("pageNumber", String.valueOf(pageNumber));
    queryParams.add("pageSize", String.valueOf(pageSize));
    // when
    List<CommentResponse> responses = mockMvcUtils.requestGet(mockMvc, requestPath, queryParams,
        author.getUserEmail(), new TypeReference<>() {
        });
    // then
    assertThat(responses).hasSize(pageSize);
  }

  @Test
  @DisplayName("success test of get comment by id")
  void getComment() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    String requestPath = String.format("/api/posts/comments/%d", target.getCommentId());
    User client = postTestDataGenerator.users.get(0);
    // when
    CommentResponse response = mockMvcUtils.requestGet(mockMvc, requestPath, null,
        client.getUserEmail(), new TypeReference<>() {
        });
    // then
    assertThat(response).isNotNull();
    assertThat(response.getCommentId()).isEqualTo(target.getCommentId());
  }

  @Test
  @DisplayName("success test of create new comment")
  void createComment() throws Exception {
    // given
    String content = UUID.randomUUID().toString();
    User author = postTestDataGenerator.users.get(0);
    Post post = postTestDataGenerator.posts.get(0);
    String requestPath = String.format("/api/posts/%d/comments", post.getPostId());
    CommentCreateDto requestBody = CommentCreateDto.builder()
        .content(content)
        .userEmail(author.getUserEmail())
        .build();
    // when
    CommentResponse response = mockMvcUtils.requestPost(mockMvc, requestPath, null,
        author.getUserEmail(), requestBody, new TypeReference<>() {
        });
    // then
    assertThat(response).isNotNull();
    assertThat(commentRepository.findById(response.getCommentId())).isPresent();
  }

  @Test
  @DisplayName("success test of update comment")
  void updateComment() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    User author = target.getUser();
    String requestPath = String.format("/api/posts/comments/%d", target.getCommentId());
    CommentUpdateDto requestBody = CommentUpdateDto.builder()
        .content(UUID.randomUUID().toString())
        .build();
    // when
    CommentResponse response = mockMvcUtils.requestPut(mockMvc, requestPath, null,
        author.getUserEmail(), requestBody, new TypeReference<>() {
        });
    // then
    assertThat(target.getCommentId()).isEqualTo(response.getCommentId());
    assertThat(requestBody.getContent()).isEqualTo(response.getContent());
  }

  @Test
  @DisplayName("success test of delete comment")
  void deleteComment() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    User author = target.getUser();
    String requestPath = String.format("/api/posts/comments/%d", target.getCommentId());
    // when
    mockMvcUtils.requestDelete(mockMvc, requestPath, null, author.getUserEmail(), null,
        new TypeReference<Void>() {
        });
    // then
    assert commentRepository.findById(target.getCommentId()).isEmpty();
  }
}