package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.config.EnableTestProfile;
import com.komentum.post.domain.Category;
import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryResponseDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.test.CategoryPostDataGenerator;
import com.komentum.test.MockMvcUtils;
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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
public class CategoryControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private CategoryPostDataGenerator categoryPostDataGenerator;

  @Autowired
  private CategoryRepository categoryRepository;

  @BeforeEach
  public void setUp() {
    categoryPostDataGenerator.deleteAllData();
    categoryPostDataGenerator.generateAllData(5, 5, 5, 5);
  }

  @AfterEach
  public void tearDown() {
    categoryPostDataGenerator.deleteAllData();
  }

  public void assertCategoryResponseDto(CategoryResponseDto categoryResponseDto) {
    Category expected = categoryRepository.findById(categoryResponseDto.getCategoryId())
        .orElse(null);
    assertThat(expected).isNotNull();
    assertThat(expected.getName()).isEqualTo(categoryResponseDto.getName());
  }

  @Test
  @DisplayName("when send request, then return categories that user has")
  public void findAllByUser_success() throws Exception {
    // given
    String clientEmail = categoryPostDataGenerator.getUsers().get(0).getUserEmail();
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("user_email", clientEmail);
    // when
    List<CategoryResponseDto> response = mockMvcUtils.requestGet(mockMvc, "/api/categories", params,
        clientEmail,
        new TypeReference<>() {
        });
    // then
    assertThat(response).isNotNull()
        .hasSize(categoryRepository.findAllByOwner_UserEmail(clientEmail).size());
    response.forEach(this::assertCategoryResponseDto);
  }

  @Test
  @DisplayName("when send request, then save category")
  public void saveCategory_success() throws Exception {
    // given
    String clientEmail = categoryPostDataGenerator.getUsers().get(0).getUserEmail();
    CategoryCreateDto createDto = CategoryCreateDto.builder()
        .name(UUID.randomUUID().toString())
        .build();
    // when
    CategoryResponseDto response = mockMvcUtils.requestPost(mockMvc, "/api/categories", null,
        clientEmail, createDto, new TypeReference<>() {
        });
    // then
    assertCategoryResponseDto(response);
  }

  @Test
  @DisplayName("when send request, then update category")
  public void updateCategory_success() throws Exception {
    // given
    Category targetCategory = categoryPostDataGenerator.getCategories().get(0);
    String clientEmail = targetCategory.getOwner().getUserEmail();
    CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
        .name(UUID.randomUUID().toString())
        .build();
    String requestPath = String.format("/api/categories/%d", targetCategory.getCategoryId());
    // when
    CategoryResponseDto response = mockMvcUtils.requestPatch(mockMvc, requestPath, null,
        clientEmail, updateDto, new TypeReference<>() {
        });
    // then
    assertCategoryResponseDto(response);
    assertThat(response.getName()).isEqualTo(updateDto.getName());
  }

  @Test
  @DisplayName("when send request, then delete category")
  public void deleteCategory_success() throws Exception {
    // given
    Category targetCategory = categoryPostDataGenerator.getCategories().get(0);
    String clientEmail = targetCategory.getOwner().getUserEmail();
    String requestPath = String.format("/api/categories/%d", targetCategory.getCategoryId());
    // when
    mockMvcUtils.requestDelete(mockMvc, requestPath, null, clientEmail, null,
        new TypeReference<Void>() {
        });
    // then
    assertThat(categoryRepository.findById(targetCategory.getCategoryId()).orElse(null)).isNull();
  }
}
