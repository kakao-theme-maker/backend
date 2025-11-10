package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.config.EnableTestProfile;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.ThemeBoardDataGenerator;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.Arrays;
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
class ThemeBoardControllerTest {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ThemeBoardDataGenerator themeBoardDataGenerator;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @BeforeEach
  void setUp() {
    themeBoardDataGenerator.deleteThemeBoards();
    themeBoardDataGenerator.generateThemeBoards(5, 2, 2);
  }

  @AfterEach
  void tearDown() {
    themeBoardDataGenerator.deleteThemeBoards();
  }

  @Test
  @DisplayName("when send request with page number and size, then return proper board data list")
  void getPosts_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 5;
    String requestPath = "/api/theme-boards";
    User client = themeBoardDataGenerator.getUsers().get(0);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("pageNumber", String.valueOf(pageNumber));
    params.add("pageSize", String.valueOf(pageSize));
    // when
    List<ThemeBoardDetailDto> responses = mockMvcUtils.requestGet(mockMvc, requestPath,
        params, client.getUserEmail(), new TypeReference<>() {
        });
    // then
    assertThat(responses).hasSize(pageSize);
  }

  @Test
  @DisplayName("success test of create post")
  void createPost_success() throws Exception {
    // given
    String requestPath = "/api/theme-boards";
    User author = themeBoardDataGenerator.getUsers().get(0);
    ThemeComponent nonThemeBoardTheme = themeBoardDataGenerator.getNonThemeBoardThemeComponents()
        .get(0);
    String[] tags = new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()};
    ThemeBoardCreateDto requestBody = ThemeBoardCreateDto.builder()
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .userEmail(author.getUserEmail())
        .themeComponentId(nonThemeBoardTheme.getThemeComponentId())
        .postTags(Arrays.stream(tags).map(t -> TagCreateDto.builder().tagName(t).build()).toList())
        .build();
    // when
    ThemeBoardDetailDto response = mockMvcUtils.requestPost(mockMvc, requestPath, null,
        author.getUserEmail(), requestBody, new TypeReference<>() {
        });
    // then : DB 저장 여부
    assertThat(postRepository.findById(response.getBoardId()))
        .isPresent();
    // then : 필드 검증
    assertThat(response.getTags())
        .extracting(TagResponse::getTagName)
        .containsExactlyInAnyOrder(tags);
    assertThat(response.getTitle())
        .isEqualTo(requestBody.getTitle());
  }

  @Test
  @DisplayName("success test of update post")
  void updatePost() throws Exception {
    // given
    Post toUpdate = themeBoardDataGenerator.getPosts().get(0);
    String requestPath = String.format("/api/theme-boards/%d", toUpdate.getPostId());
    User author = toUpdate.getUser();
    String[] tags = new String[]{UUID.randomUUID().toString(), UUID.randomUUID().toString()};
    ThemeBoardUpdateDto requestBody = ThemeBoardUpdateDto.builder()
        .title(UUID.randomUUID().toString())
        .postTags(Arrays.stream(tags).map(t -> TagUpdateDto.builder().tagName(t).build()).toList())
        .userEmail(author.getUserEmail())
        .build();
    // when
    ThemeBoardDetailDto response = mockMvcUtils.requestPut(mockMvc, requestPath, null,
        author.getUserEmail(), requestBody, new TypeReference<>() {
        });
    // then : 필드 검증
    assertThat(response.getTitle())
        .isEqualTo(requestBody.getTitle());
    assertThat(response.getTags())
        .extracting(TagResponse::getTagName)
        .containsExactlyInAnyOrder(tags);
  }

  @Test
  @DisplayName("success test of delete post")
  void deletePost() throws Exception {
    // given
    Post toDelete = themeBoardDataGenerator.getPosts().get(0);
    User author = toDelete.getUser();
    String requestPath = String.format("/api/theme-boards/%d", toDelete.getPostId());
    // when
    mockMvcUtils.requestDelete(mockMvc, requestPath, null, author.getUserEmail(), null,
        new TypeReference<Void>() {
        });
    // then
    assertThat(postRepository.findById(toDelete.getPostId()))
        .isEmpty();
  }
}