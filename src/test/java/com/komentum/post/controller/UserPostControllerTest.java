package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
public class UserPostControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private PostRepository postRepository;

  @Autowired
  FileManager fileManager;

  @Autowired
  MockMvcUtils mockMvcUtils;

  @Autowired
  TestDataRemover testDataRemover;

  @Autowired
  PostScenarioSupport postScenarioSupport;

  int postPerUser = 5;
  int bookmarkedPostsPerUser;
  int prefersPerUser;
  PostScenarioSupport.Result result;

  @BeforeEach
  void setUp() {
    int userCount = 3;
    int prefersPerPost = 3;
    // generate data
    result = postScenarioSupport.builder()
        .withUsers(userCount)
        .withThemeBoardPerUser(postPerUser)
        .withPrefersPerPost(prefersPerPost)
        .withBookmarkRatio(1)
        .build();
    // set values
    bookmarkedPostsPerUser = postPerUser * userCount;
    prefersPerUser = postPerUser * prefersPerPost;
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  public void assertUserPostListResponseDto(UserPostListResponseDto responseDto,
      String previewImageUrl) {
    // post ID assertion
    assertThat(responseDto.getPostId()).isNotNull();
    // DB assertion
    Post post = postRepository.findById(responseDto.getPostId()).orElse(null);
    assertThat(post).isNotNull();
    // response assertion
    assertThat(responseDto.getCreatedAt()).isNotNull();
    assertThat(responseDto.getUpdatedAt()).isNotNull();
    assertThat(responseDto.getPreviewImageUrl()).isEqualTo(previewImageUrl);
    assertThat(responseDto.getAuthorName()).isEqualTo(post.getUser().getName());
    assertThat(responseDto.getAuthorProfileImageUrl()).isEqualTo(post.getUser().getProfileImg());
    assertThat(responseDto.getPostType()).isEqualTo(post.getPostType());
  }

  @Test
  @DisplayName("유저가 작성한 게시글 목록 조회")
  void getUserPostTest() throws Exception {
    //given
    User targetUser = result.getFirstUser();
    String expectedPreviewImageUrl = String.format("http://mocked-url/%s", UUID.randomUUID());
    // stub
    given(fileManager.resolveFilePath(any()))
        .willReturn(expectedPreviewImageUrl);
    //when
    List<UserPostListResponseDto> result = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<UserPostListResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/upload-posts")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(targetUser))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    //then
    assertThat(result).hasSize(postPerUser);
    for (UserPostListResponseDto res : result) {
      assertUserPostListResponseDto(res, expectedPreviewImageUrl);
    }
  }

  @Test
  @DisplayName("사용자가 북마크에 저장한 게시글 목록 반환")
  void findSavedPostList_success() throws Exception {
    // given
    User client = result.getFirstUser();
    String expectedPreviewImageUrl = String.format("http://mocked-url/%s", UUID.randomUUID());
    // stub
    given(fileManager.resolveFilePath(any()))
        .willReturn(expectedPreviewImageUrl);
    // when
    List<UserPostListResponseDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<UserPostListResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/bookmarked-posts")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(bookmarkedPostsPerUser);
    for (UserPostListResponseDto res : response) {
      assertUserPostListResponseDto(res, expectedPreviewImageUrl);
    }
  }

  @Test
  @DisplayName("사용자가 좋아요를 누른 게시글 목록 반환")
  void findPreferedPostList_success() throws Exception {
    // given
    User client = result.getFirstUser();
    String expectedPreviewImageUrl = String.format("http://mocked-url/%s", UUID.randomUUID());
    // stub
    given(fileManager.resolveFilePath(any()))
        .willReturn(expectedPreviewImageUrl);
    // when
    List<UserPostListResponseDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<UserPostListResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/preferred-posts")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(prefersPerUser);
    for (UserPostListResponseDto res : response) {
      assertUserPostListResponseDto(res, expectedPreviewImageUrl);
    }
  }

}