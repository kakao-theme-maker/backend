package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.BoardDetailDataGenerator;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
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

  private final int maxPreferPerPost = 5;

  @BeforeEach
  void setUp() {
    boardDetailDataGenerator.deleteThemeBoards();
    boardDetailDataGenerator.generateThemeBoards(5, 2, 2, maxPreferPerPost);
  }

  @AfterEach
  void tearDown() {
    boardDetailDataGenerator.deleteThemeBoards();
  }

  public void assertThemeBoardPreview(ThemeBoardPreviewDto dto) {
    assertThat(dto.getPostId()).isNotNull();
    assertThat(dto.getThemeComponentId()).isNotNull();
    assertThat(dto.getPrefers()).isGreaterThanOrEqualTo(0);
    assertThat(dto.getTitle()).isNotBlank();
    assertThat(dto.getPreviewImageUrl()).isNotBlank();
    assertThat(dto.getCreatedAt()).isNotBlank();
  }

  public void assertThemeBoardPreviewList(List<ThemeBoardPreviewDto> previewDtoList) {
    for (ThemeBoardPreviewDto dto : previewDtoList) {
      assertThemeBoardPreview(dto);
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
  @DisplayName("페이지 기반 테마 게시글 목록 조회 성공 테스트")
  void getPosts_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 5;
    String requestPath = "/api/theme-boards";
    User client = boardDetailDataGenerator.getUsers().get(0);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardPreviewDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemeBoardPreviewDto>>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .params(TestParams.withPaging(pageNumber, pageSize))
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(pageSize);
    assertThemeBoardPreviewList(response);
  }

  @Test
  @DisplayName("인기 테마 게시글 목록 페이징 기반 조회 성공 테스트")
  void findPopularThemeBoards_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 5;
    User client = boardDetailDataGenerator.getUsers().get(0);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardPreviewDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemeBoardPreviewDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/theme-boards/popular")
            .params(TestParams.withPaging(pageNumber, pageSize))
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(pageSize);
    long lastPrefer = response.get(0).getPrefers();
    assertThat(lastPrefer).isEqualTo(maxPreferPerPost);
    for (ThemeBoardPreviewDto res : response) {
      assertThat(res.getPrefers()).isLessThanOrEqualTo(lastPrefer);
      assertThemeBoardPreview(res);
    }
  }

  @Test
  @DisplayName("추천 테마 게시글 목록 페이징 기반 조회 성공 테스트 ( 임시 기능 )")
  void findRecommendedThemeBoards_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 5;
    User client = boardDetailDataGenerator.getUsers().get(0);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardPreviewDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemeBoardPreviewDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/theme-boards/recommended")
            .params(TestParams.withPaging(pageNumber, pageSize))
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThat(response).hasSize(pageSize);
    assertThemeBoardPreviewList(response);
  }

  @Test
  @DisplayName("테마 게시글 상세 조회 성공 테스트")
  void findThemeBoardByPostId_success() throws Exception {
    // given
    ThemeBoard targetThemeBoard = boardDetailDataGenerator.getThemeBoards().get(0);
    Long postId = targetThemeBoard.getPost().getPostId();
    User client = boardDetailDataGenerator.getUsers().get(0);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    ThemeBoardDetailDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, ThemeBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/theme-boards/%d", postId))
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThemeBoardDetail(response);
  }

  @Test
  @DisplayName("테마 게시글 생성 성공 테스트")
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
    ThemeBoardDetailDto response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<ThemeBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.POST)
            .clientDto(TestClientDto.fromEntity(author))
            .formDataList(formDataList)
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThemeBoardDetail(response);
  }

  @Test
  @DisplayName("테마 게시글 수정 성공 테스트")
  void updatePost_success() throws Exception {
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
    ThemeBoardDetailDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<ThemeBoardUpdateDto, ThemeBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(author))
            .body(requestBody)
            .responseType(new TypeReference<>() {
            })
            .statusCode(200)
            .build()
    );
    // then : 필드 검증
    assertThemeBoardDetail(response);
  }

  @Test
  @DisplayName("테마 게시글 삭제 성공 테스트")
  void deletePost_success() throws Exception {
    // given
    Post toDelete = boardDetailDataGenerator.getPosts().get(0);
    User author = toDelete.getUser();
    String requestPath = String.format("/api/theme-boards/%d", toDelete.getPostId());
    // when
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path(requestPath)
            .httpMethod(HttpMethod.DELETE)
            .clientDto(TestClientDto.fromEntity(author))
            .responseType(new TypeReference<>() {
            })
            .statusCode(204)
            .build()
    );
    // then
    assertThat(postRepository.findById(toDelete.getPostId()))
        .isEmpty();
  }
}