package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.config.EnableTestProfile;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.BoardDetailDataGenerator;
import com.komentum.test.MockMvcUtils;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
  private BoardDetailDataGenerator boardDetailDataGenerator;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private FileManager fileManager;

  @BeforeEach
  void setUp() {
    boardDetailDataGenerator.deleteThemeBoards();
    boardDetailDataGenerator.generateThemeBoards(5, 2, 2);
  }

  @AfterEach
  void tearDown() {
    boardDetailDataGenerator.deleteThemeBoards();
  }

  public void assertThemeBoardPreviewList(List<ThemeBoardPreviewDto> previewDtoList) {
    for (ThemeBoardPreviewDto dto : previewDtoList) {
      assertThat(dto.getPostId()).isNotNull();
      assertThat(dto.getThemeComponentId()).isNotNull();
      assertThat(dto.getPrefers()).isGreaterThanOrEqualTo(0);
      assertThat(dto.getTitle()).isNotBlank();
      assertThat(dto.getPreviewImageUrl()).isNotBlank();
      assertThat(dto.getCreatedAt()).isNotBlank();
    }
  }

  public void assertThemeBoardDetail(ThemeBoardDetailDto detailDto) {
    Post savedPost = postRepository.findById(detailDto.getPostId()).orElse(null);
    assertThat(savedPost).isNotNull();
    assertThat(detailDto.getThemeComponentId()).isNotNull();
    assertThat(detailDto.getPreviewImageUrl()).isNotBlank();
    assertThat(detailDto.getContent()).isEqualTo(savedPost.getContent());
    assertThat(detailDto.getTitle()).isEqualTo(savedPost.getTitle());
    assertThat(detailDto.getCreatedAt()).isNotBlank();
    assertThat(detailDto.getUserEmail()).isNotBlank();
    assertThat(detailDto.getPrefers()).isGreaterThanOrEqualTo(0);
  }

  @Test
  @DisplayName("when send request with page number and size, then return proper board data list")
  void getPosts_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 5;
    String requestPath = "/api/theme-boards";
    User client = boardDetailDataGenerator.getUsers().get(0);
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("pageNumber", String.valueOf(pageNumber));
    params.add("pageSize", String.valueOf(pageSize));
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardPreviewDto> response = mockMvcUtils.requestGet(mockMvc, requestPath,
        params, client.getPublicUserId(), new TypeReference<>() {
        });
    // then
    assertThat(response).hasSize(pageSize);
    assertThemeBoardPreviewList(response);
  }

  @Test
  @DisplayName("success test of create post")
  void createPost_success() throws Exception {
    // given
    String requestPath = "/api/theme-boards";
    User author = boardDetailDataGenerator.getUsers().get(0);
    ThemeComponent nonThemeBoardTheme = boardDetailDataGenerator.getNonThemeBoardThemeComponents()
        .get(0);
    String testPreviewImageUrl = UUID.randomUUID().toString();
    ThemeBoardCreateDto createDto = ThemeBoardCreateDto.builder()
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .themeComponentId(nonThemeBoardTheme.getThemeComponentId())
        .publicFlag(true)
        .build();
    MockMultipartFile testPreviewImage = mockMvcUtils.fileToTestFormData("preview_image",
        "preview_image.png",
        MediaType.IMAGE_PNG, "test data".getBytes());
    MockMultipartFile boardInfo = mockMvcUtils.jsonToTestFormData("board_info",
        createDto);
    List<MockMultipartFile> formDataList = List.of(testPreviewImage, boardInfo);
    // stub
    Mockito.when(fileManager.uploadFile(any(), any()))
        .thenReturn(testPreviewImage.getOriginalFilename());
    Mockito.when(fileManager.resolveFilePath(any()))
        .thenReturn(testPreviewImageUrl);
    // when
    ThemeBoardDetailDto response = mockMvcUtils.performMultipartRequest(mockMvc, requestPath,
        HttpMethod.POST, null,
        author.getPublicUserId(), formDataList, new TypeReference<>() {
        });
    // then
    assertThemeBoardDetail(response);
  }

  @Test
  @DisplayName("success test of update post")
  void updatePost() throws Exception {
    // given
    Post toUpdate = boardDetailDataGenerator.getPosts().get(0);
    String requestPath = String.format("/api/theme-boards/%d", toUpdate.getPostId());
    User author = toUpdate.getUser();
    ThemeBoardUpdateDto requestBody = ThemeBoardUpdateDto.builder()
        .title(UUID.randomUUID().toString())
        .build();
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    ThemeBoardDetailDto response = mockMvcUtils.requestPut(mockMvc, requestPath, null,
        author.getPublicUserId(), requestBody, new TypeReference<>() {
        });
    // then : 필드 검증
    assertThemeBoardDetail(response);
  }

  @Test
  @DisplayName("success test of delete post")
  void deletePost() throws Exception {
    // given
    Post toDelete = boardDetailDataGenerator.getPosts().get(0);
    User author = toDelete.getUser();
    String requestPath = String.format("/api/theme-boards/%d", toDelete.getPostId());
    // when
    mockMvcUtils.requestDelete(mockMvc, requestPath, null, author.getPublicUserId(), null,
        new TypeReference<Void>() {
        });
    // then
    assertThat(postRepository.findById(toDelete.getPostId()))
        .isEmpty();
  }
}