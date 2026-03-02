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
import com.komentum.test.data.CategoryPostDataGenerator;
import com.komentum.test.data.PostTestDataGenerator;
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
  private PostTestDataGenerator postTestDataGenerator;

  @Autowired
  private CategoryPostDataGenerator categoryPostDataGenerator;

  @Autowired
  MockMvcUtils mockMvcUtils;

  private final int postPerUser = 6;
  private final int postPerCategory = 6;
  private final int categoryPerUser = 1;

  @BeforeEach
  void setUp() {
    categoryPostDataGenerator.deleteAllData();
    categoryPostDataGenerator.generateCategoriesAndPosts(1, postPerUser, 5, categoryPerUser,
        postPerCategory);
  }

  @AfterEach
  void tearDown() {
    categoryPostDataGenerator.deleteAllData();
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
  }

  @Test
  @DisplayName("유저가 작성한 게시글 목록 조회")
  void getUserPostTest() throws Exception {
    //given
    User targetUser = postTestDataGenerator.getUsers().get(0);
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
  @DisplayName("사용자가 카테고리에 저장한 게시글 목록 반환")
  void findSavedPostList_success() throws Exception {
    // given
    User client = postTestDataGenerator.getUsers().get(0);
    String expectedPreviewImageUrl = String.format("http://mocked-url/%s", UUID.randomUUID());
    // stub
    given(fileManager.resolveFilePath(any()))
        .willReturn(expectedPreviewImageUrl);
    // when
    List<UserPostListResponseDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<UserPostListResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/users/me/saved-posts")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(categoryPerUser * postPerCategory);
    for (UserPostListResponseDto res : response) {
      assertUserPostListResponseDto(res, expectedPreviewImageUrl);
    }
  }

}