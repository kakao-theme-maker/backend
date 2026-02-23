package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CategoryPostDto.CategoryPostResponse;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.CategoryPostDataGenerator;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.user.domain.User;
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
public class CategoryPostControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private CategoryPostDataGenerator categoryPostDataGenerator;

  @Autowired
  private CategoryPostRepository categoryPostRepository;

  @BeforeEach
  public void setUp() {
    categoryPostDataGenerator.deleteAllData();
    categoryPostDataGenerator.generateCategoriesAndPosts(5, 5, 5, 5, 5);
  }

  @AfterEach
  public void tearDown() {
    categoryPostDataGenerator.deleteAllData();
  }

  private void assertCategoryPostResponse(CategoryPostResponse response, Category targetCategory,
      Post targetPost) {
    CategoryPost savedData = categoryPostRepository.findByCategory_CategoryIdAndPost_PostId(
        targetCategory.getCategoryId(), targetPost.getPostId()).orElse(null);
    assertThat(savedData).isNotNull();
    assertThat(response.getCategoryId()).isEqualTo(targetCategory.getCategoryId());
    assertThat(response.getPostId()).isEqualTo(targetPost.getPostId());
  }

  @Test
  @DisplayName("when send request, then register new category-post data")
  public void registerPostOnCategory_whenNotExists_createsRelation() throws Exception {
    // given
    User client = categoryPostDataGenerator.getUsers().get(0);
    Category targetCategory = categoryPostDataGenerator.getCategories().get(0);
    Post targetPost = categoryPostDataGenerator.getCategoryUnregisteredPosts().get(0);
    String requestPath = String.format("/api/categories/%d/posts/%d",
        targetCategory.getCategoryId(), targetPost.getPostId());
    // when
    CategoryPostResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, CategoryPostResponse>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertCategoryPostResponse(response, targetCategory, targetPost);
  }

  @Test
  @DisplayName("when send request, then use saved category-post data")
  public void registerPostOnCategory_whenExists_returnsExisting() throws Exception {
    // given
    User client = categoryPostDataGenerator.getUsers().get(0);
    CategoryPost savedCategoryPost = categoryPostDataGenerator.getCategoryPosts().get(0);
    Category targetCategory = savedCategoryPost.getCategory();
    Post targetPost = savedCategoryPost.getPost();
    String requestPath = String.format("/api/categories/%d/posts/%d",
        targetCategory.getCategoryId(), targetPost.getPostId());
    // when
    CategoryPostResponse response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, CategoryPostResponse>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(client))
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertCategoryPostResponse(response, targetCategory, targetPost);
  }

  @Test
  @DisplayName("when send request, then delete category-post data")
  public void deletePostFromCategory_success() throws Exception {
    // given
    User client = categoryPostDataGenerator.getUsers().get(0);
    CategoryPost savedCategoryPost = categoryPostDataGenerator.getCategoryPosts().get(0);
    Category targetCategory = savedCategoryPost.getCategory();
    Post targetPost = savedCategoryPost.getPost();
    String requestPath = String.format("/api/categories/%d/posts/%d",
        targetCategory.getCategoryId(), targetPost.getPostId());
    // when
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.DELETE)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(204)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(categoryPostRepository.findByCategory_CategoryIdAndPost_PostId(
        targetCategory.getCategoryId(), targetPost.getPostId())).isEmpty();
  }
}
