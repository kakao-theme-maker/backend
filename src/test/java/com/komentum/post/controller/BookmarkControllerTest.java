package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.seed.seeder.Scenario.PostScenarioSupport;
import com.komentum.seed.seeder.Scenario.PostScenarioSupport.Result;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class BookmarkControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostScenarioSupport postScenarioSupport;

  @Autowired
  private TestDataRemover testDataRemover;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private CategoryPostRepository categoryPostRepository;

  private Result postScenarioResult;

  @BeforeEach
  public void setUp() {
    postScenarioResult = postScenarioSupport.builder()
        .withUsers(3)
        .withPostPerUser(5)
        .build();
  }

  @AfterEach
  public void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("when send request, add post on bookmark category")
  void addPostOnBookmark_success() throws Exception {
    // given
    User client = postScenarioResult.getFirstUser();
    Post postToAddonBookmark = postScenarioResult.posts().get(0);
    // when
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/bookmarks/posts/%d", postToAddonBookmark.getPostId()))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .statusCode(200)
            .build()
    );
    // then: bookmark 카테고리 유무 검증
    Optional<Category> bookmarkCategory = categoryRepository
        .findByCategoryTypeAndOwner(CategoryType.BOOKMARK, client);
    assertThat(bookmarkCategory).isNotEmpty();
    // then: bookmark에 post가 들어있는지 검증
    assertThat(categoryPostRepository.findByCategory_CategoryIdAndPost_PostId(
        bookmarkCategory.get().getCategoryId(),
        postToAddonBookmark.getPostId()
    )).isNotEmpty();
  }

  @Test
  @DisplayName("If posts are stored in bookmarks in duplicate, maintain the existing state")
  void addPostOnBookmark_whenExists_returnsExisting() throws Exception {
    // given
    User client = postScenarioResult.getFirstUser();
    Post postToAdd = postScenarioResult.posts().get(0);
    // when: 북마크에 게시글을 추가한다
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/bookmarks/posts/%d", postToAdd.getPostId()))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .statusCode(200)
            .build()
    );
    // when: 동일한 게시글을 북마크에 다시 추가한다
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/bookmarks/posts/%d", postToAdd.getPostId()))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .statusCode(200)
            .build()
    );
    // then: bookmark 카테고리 유무 검증
    Optional<Category> bookmarkCategory = categoryRepository
        .findByCategoryTypeAndOwner(CategoryType.BOOKMARK, client);
    assertThat(bookmarkCategory).isNotEmpty();
    // then: bookmark에 post가 들어있는지 검증
    assertThat(categoryPostRepository.findByCategory_CategoryIdAndPost_PostId(
        bookmarkCategory.get().getCategoryId(),
        postToAdd.getPostId()
    )).isNotEmpty();
  }

  @Test
  @DisplayName("If remove non-existent posts from bookmarks, maintain the existing state")
  void deletePostFromBookmark_whenNotExists_returnsNoContent() throws Exception {
    // given
    User client = postScenarioResult.getFirstUser();
    Post targetPost = postScenarioResult.posts().get(0);
    // when: bookmark에서 post 제거
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/bookmarks/posts/%d", targetPost.getPostId()))
            .httpMethod(HttpMethod.DELETE)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .statusCode(204)
            .build()
    );
    // then: bookmark 카테고리가 없음을 확인
    Optional<Category> bookmarkCategory = categoryRepository
        .findByCategoryTypeAndOwner(CategoryType.BOOKMARK, client);
    assertThat(bookmarkCategory).isEmpty();
  }

  @Test
  @DisplayName("when send request, delete post from bookmark category")
  void deletePostFromBookmark_success() throws Exception {
    // given
    User client = postScenarioResult.getFirstUser();
    Post targetPost = postScenarioResult.posts().get(0);
    // when: bookmark에 post 등록
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/bookmarks/posts/%d", targetPost.getPostId()))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .statusCode(200)
            .build()
    );
    // when: bookmark에서 post 제거
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/bookmarks/posts/%d", targetPost.getPostId()))
            .httpMethod(HttpMethod.DELETE)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .statusCode(204)
            .build()
    );
    // then: bookmark 카테고리 유무 검증
    Optional<Category> bookmarkCategory = categoryRepository
        .findByCategoryTypeAndOwner(CategoryType.BOOKMARK, client);
    assertThat(bookmarkCategory).isNotEmpty();
    // then: bookmark에 post가 들어있는지 검증
    assertThat(categoryPostRepository.findByCategory_CategoryIdAndPost_PostId(
        bookmarkCategory.get().getCategoryId(),
        targetPost.getPostId()
    )).isEmpty();
  }
}