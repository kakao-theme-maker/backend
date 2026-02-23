package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.post.domain.Category;
import com.komentum.post.dto.CategoryDto.CategoryCreateDto;
import com.komentum.post.dto.CategoryDto.CategoryResponseDto;
import com.komentum.post.dto.CategoryDto.CategoryUpdateDto;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.CategoryPostDataGenerator;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
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
    categoryPostDataGenerator.generateCategoriesAndPosts(5, 5, 5, 5, 5);
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
    User client = categoryPostDataGenerator.getUsers().get(0);
    MultiValueMap<String, String> params = TestParams.withEmpty();
    params.add(TestParams.USER_EMAIL, client.getUserEmail());
    // when
    List<CategoryResponseDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<CategoryResponseDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/categories")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .params(params)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response)
        .isNotNull()
        .hasSize(categoryRepository.findAllByOwner_UserEmail(client.getUserEmail()).size());
    response.forEach(this::assertCategoryResponseDto);
  }

  @Test
  @DisplayName("when send request, then save category")
  public void saveCategory_success() throws Exception {
    // given
    User client = categoryPostDataGenerator.getUsers().get(0);
    CategoryCreateDto createDto = CategoryCreateDto.builder()
        .name(UUID.randomUUID().toString())
        .build();
    // when
    CategoryResponseDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<CategoryCreateDto, CategoryResponseDto>builder()
            .mockMvc(mockMvc)
            .path("/api/categories")
            .httpMethod(HttpMethod.POST)
            .clientDto(TestClientDto.fromEntity(client))
            .body(createDto)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertCategoryResponseDto(response);
  }

  @Test
  @DisplayName("when send request, then update category")
  public void updateCategory_success() throws Exception {
    // given
    Category targetCategory = categoryPostDataGenerator.getCategories().get(0);
    User client = targetCategory.getOwner();
    CategoryUpdateDto updateDto = CategoryUpdateDto.builder()
        .name(UUID.randomUUID().toString())
        .build();
    String requestPath = String.format("/api/categories/%d", targetCategory.getCategoryId());
    // when
    CategoryResponseDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<CategoryUpdateDto, CategoryResponseDto>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.PATCH)
            .clientDto(TestClientDto.fromEntity(client))
            .body(updateDto)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertCategoryResponseDto(response);
    assertThat(response.getName()).isEqualTo(updateDto.getName());
  }

  @Test
  @DisplayName("when send request, then delete category")
  public void deleteCategory_success() throws Exception {
    // given
    Category targetCategory = categoryPostDataGenerator.getCategories().get(0);
    User client = targetCategory.getOwner();
    String requestPath = String.format("/api/categories/%d", targetCategory.getCategoryId());
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
    assertThat(categoryRepository.findById(targetCategory.getCategoryId())).isEmpty();
  }
}
