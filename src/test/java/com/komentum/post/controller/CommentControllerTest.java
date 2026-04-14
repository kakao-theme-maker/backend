package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.post.domain.Comment;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CommentDto.CommentCreateDto;
import com.komentum.post.dto.CommentDto.CommentResponse;
import com.komentum.post.dto.CommentDto.CommentUpdateDto;
import com.komentum.post.repository.CommentLikeRepository;
import com.komentum.post.repository.CommentRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.PostTestDataGenerator;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class CommentControllerTest {

  @Autowired
  private CommentRepository commentRepository;

  @Autowired
  private CommentLikeRepository commentLikeRepository;

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

  public void assertCommentResponse(CommentResponse target) {
    // DB assertion
    Comment saved = commentRepository.findById(target.getCommentId()).orElse(null);
    assertThat(saved).isNotNull();
    // target assertion
    assertThat(target.getContent()).isEqualTo(saved.getContent());
    assertThat(target.getUserEmail()).isEqualTo(saved.getUser().getUserEmail());
    assertThat(target.getUserName()).isEqualTo(saved.getUser().getName());
    assertThat(target.getProfileImageUrl()).isEqualTo(saved.getUser().getProfileImg());
    assertThat(target.getLikeCount()).isEqualTo(saved.getLikeCount());
    assertThat(target.getCreatedAt()).isNotBlank();
  }

  @Test
  @DisplayName("success test of get comments by page")
  void getComments_success() throws Exception {
    // given
    Post post = postTestDataGenerator.posts.get(0);
    User author = postTestDataGenerator.users.get(0);
    String requestPath = String.format("/api/posts/%d/comments", post.getPostId());
    int pageSize = 5;
    MultiValueMap<String, String> params = TestParams.withPaging(0, pageSize);
    // when
    List<CommentResponse> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<CommentResponse>>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path(requestPath)
            .params(params)
            .clientDto(TestClientDto.fromEntity(author))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(pageSize);
    for (CommentResponse target : response) {
      assertCommentResponse(target);
    }
  }

  @Test
  @DisplayName("success test of get comment by id")
  void getComment() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    String requestPath = String.format("/api/posts/comments/%d", target.getCommentId());
    User client = postTestDataGenerator.users.get(0);
    // when
    CommentResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, CommentResponse>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path(requestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertCommentResponse(response);
  }

  @Test
  @DisplayName("success test of like comment")
  void likeComment_success() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    User client = postTestDataGenerator.users.get(0);
    String requestPath = String.format("/api/comments/%d/like", target.getCommentId());
    // when
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.POST)
            .path(requestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );

    //중복 요청
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.POST)
            .path(requestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    Comment saved = commentRepository.findById(target.getCommentId()).orElseThrow();
    assertThat(saved.getLikeCount()).isEqualTo(1L);
    assertThat(commentLikeRepository.existsByUserIdAndCommentId(
        client.getUserId(),
        target.getCommentId())).isTrue();
  }

  @Test
  @DisplayName("success test of unlike comment")
  void unlikeComment_success() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    User client = postTestDataGenerator.users.get(0);
    String requestPath = String.format("/api/comments/%d/like", target.getCommentId());
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.POST)
            .path(requestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // when

    // unlike
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.DELETE)
            .path(requestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(204)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    Comment saved = commentRepository.findById(target.getCommentId()).orElseThrow();
    assertThat(saved.getLikeCount()).isZero();
    assertThat(commentLikeRepository.existsByUserIdAndCommentId(
        client.getUserId(),
        target.getCommentId())).isFalse();
  }

  @Test
  @DisplayName("success test of get comment like count")
  void getCommentLikeCount_success() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    User client = postTestDataGenerator.users.get(0);
    String requestPath = String.format("/api/comments/%d/like", target.getCommentId());
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.POST)
            .path(requestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // when
    Long response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Long>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path(requestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).isEqualTo(1L);
  }

  @Test
  @DisplayName("success test of get liked comment")
  void getLikedComment_success() throws Exception {
    // given
    Comment target = postTestDataGenerator.comments.get(0);
    User client = postTestDataGenerator.users.get(0);
    String likeRequestPath = String.format("/api/comments/%d/like", target.getCommentId());
    String getRequestPath = String.format("/api/posts/comments/%d", target.getCommentId());
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.POST)
            .path(likeRequestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // when
    CommentResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, CommentResponse>builder()
            .mockMvc(mockMvc)
            .httpMethod(HttpMethod.GET)
            .path(getRequestPath)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response.getLikeCount()).isEqualTo(1L);
    assertThat(response.getIsLiked()).isTrue();
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
        .build();
    // when
    CommentResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<CommentCreateDto, CommentResponse>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.POST)
            .clientDto(TestClientDto.fromEntity(author))
            .body(requestBody)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertCommentResponse(response);
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
    CommentResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<CommentUpdateDto, CommentResponse>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(author))
            .body(requestBody)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
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
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.DELETE)
            .clientDto(TestClientDto.fromEntity(author))
            .statusCode(204)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assert commentRepository.findById(target.getCommentId()).isEmpty();
  }
}
