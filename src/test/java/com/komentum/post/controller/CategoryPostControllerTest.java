package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.config.EnableTestProfile;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.CategoryPostDto.CategoryPostResponse;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.test.CategoryPostDataGenerator;
import com.komentum.test.MockMvcUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    categoryPostDataGenerator.generateAllData(5, 5, 5, 5);
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
    String clientEmail = categoryPostDataGenerator.getUsers().get(0).getUserEmail();
    Category targetCategory = categoryPostDataGenerator.getCategories().get(0);
    Post targetPost = categoryPostDataGenerator.getCategoryUnregisteredPosts().get(0);
    String requestPath = String.format("/api/categories/%d/posts/%d",
        targetCategory.getCategoryId(), targetPost.getPostId());
    // when
    CategoryPostResponse response = mockMvcUtils.requestPut(mockMvc, requestPath, null, clientEmail,
        null, new TypeReference<>() {
        });
    // then
    assertCategoryPostResponse(response, targetCategory, targetPost);
  }

  @Test
  @DisplayName("when send request, then use saved category-post data")
  public void registerPostOnCategory_whenExists_returnsExisting() throws Exception {
    // given
    String clientEmail = categoryPostDataGenerator.getUsers().get(0).getUserEmail();
    CategoryPost savedCategoryPost = categoryPostDataGenerator.getCategoryPosts().get(0);
    Category targetCategory = savedCategoryPost.getCategory();
    Post targetPost = savedCategoryPost.getPost();
    String requestPath = String.format("/api/categories/%d/posts/%d",
        targetCategory.getCategoryId(), targetPost.getPostId());
    // when
    CategoryPostResponse response = mockMvcUtils.requestPut(mockMvc, requestPath, null, clientEmail,
        null, new TypeReference<>() {
        });
    // then
    assertCategoryPostResponse(response, targetCategory, targetPost);
  }

  @Test
  @DisplayName("when send request, then delete category-post data")
  public void deletePostFromCategory_success() throws Exception {
    // given
    String clientEmail = categoryPostDataGenerator.getUsers().get(0).getUserEmail();
    CategoryPost savedCategoryPost = categoryPostDataGenerator.getCategoryPosts().get(0);
    Category targetCategory = savedCategoryPost.getCategory();
    Post targetPost = savedCategoryPost.getPost();
    String requestPath = String.format("/api/categories/%d/posts/%d",
        targetCategory.getCategoryId(), targetPost.getPostId());
    // when
    mockMvcUtils.requestDelete(mockMvc, requestPath, null,
        clientEmail,
        null, new TypeReference<Void>() {
        });
    // then
    assertThat(categoryPostRepository.findByCategory_CategoryIdAndPost_PostId(
        targetCategory.getCategoryId(), targetPost.getPostId())).isEmpty();
  }
}
