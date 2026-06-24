package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.dto.TagDto.TagCreateDto;
import com.komentum.post.dto.TagDto.TagResponse;
import com.komentum.post.dto.TagDto.TagUpdateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardCreateDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardDetailDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardPreviewDto;
import com.komentum.post.dto.ThemeBoardDto.ThemeBoardUpdateDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.BoardDetailDataGenerator;
import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.test.data.MockMultipartFileUtils.ImageExtension;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.user.domain.User;
import java.time.LocalDateTime;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class ThemeBoardControllerTest {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @Autowired
  private ThemeImageRepository themeImageRepository;

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
    assertThat(detailDto.getPreviewImageUrl()).isNotNull().isNotEmpty();
    assertThat(detailDto.getContent()).isEqualTo(savedPost.getContent());
    assertThat(detailDto.getTitle()).isEqualTo(savedPost.getTitle());
    assertThat(detailDto.getCreatedAt()).isNotBlank();
    assertThat(detailDto.getUserEmail()).isNotBlank();
    assertThat(detailDto.getPrefers()).isGreaterThanOrEqualTo(0);
    assertThat(detailDto.getComments()).isGreaterThanOrEqualTo(0);
    assertThat(detailDto.getUserName()).isNotNull();
  }

  private List<ThemeBoardDetailDto> requestThemeBoardDetails(
      MultiValueMap<String, String> params, User client) throws Exception {
    return mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemeBoardDetailDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/theme-boards/details")
            .params(params)
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
  }

  private List<ThemeBoardPreviewDto> requestThemeBoardPreviews(
      MultiValueMap<String, String> params, User client) throws Exception {
    return mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemeBoardPreviewDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/theme-boards")
            .params(params)
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
  }

  private ComponentType getComponentType(TypeCode typeCode) {
    return componentTypeRepository.findAllByTypeCodeIn(List.of(typeCode)).get(0);
  }

  private void leaveTypeCodeOnTargetThemeOnly(TypeCode typeCode, ThemeBoard targetThemeBoard) {
    List<Integer> themeComponentIds = boardDetailDataGenerator.getThemeBoards()
        .stream()
        .map(themeBoard -> themeBoard.getThemeComponent().getThemeComponentId())
        .toList();
    List<ThemeImage> imagesToDelete = themeImageRepository
        .fetchJoinByThemeComponentAndTypeCode(themeComponentIds, typeCode)
        .stream()
        .filter(themeImage -> !themeImage.getThemeComponent().getThemeComponentId()
            .equals(targetThemeBoard.getThemeComponent().getThemeComponentId()))
        .toList();
    themeImageRepository.deleteAll(imagesToDelete);
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
  @DisplayName("If a pinned post ID is provided, place that post at the top of the first page and return only theme boards written by the same author.")
  void findThemeBoardDetails_ifPinnedPostIdExists() throws Exception {
    // given
    User client = boardDetailDataGenerator.getUsers().get(0);
    Post pinnedPost = boardDetailDataGenerator.getThemeBoards().get(0).getPost();
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardDetailDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemeBoardDetailDto>>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/theme-boards/details?pinned_post_id=%d&page=0",
                pinnedPost.getPostId()))
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build());
    // then
    assertThat(response).isNotEmpty();
    assertThat(response.get(0).getPostId()).isEqualTo(pinnedPost.getPostId());
    for (ThemeBoardDetailDto dto : response) {
      assertThemeBoardDetail(dto);
    }
  }

  @Test
  @DisplayName("keyword 검색 조건에 매칭되는 테마 게시글 목록 정보를 최상단에 반환한다.")
  void findThemeBoards_keywordMatchedPostFirst() throws Exception {
    // given
    User client = boardDetailDataGenerator.getUsers().get(0);
    ThemeBoard targetThemeBoard = boardDetailDataGenerator.getThemeBoards().get(0);
    ThemeBoard contentMatchedThemeBoard = boardDetailDataGenerator.getThemeBoards().get(
        boardDetailDataGenerator.getThemeBoards().size() - 1);
    Post targetPost = targetThemeBoard.getPost();
    Post contentMatchedPost = contentMatchedThemeBoard.getPost();
    String keyword = "theme-keyword-" + UUID.randomUUID();
    targetPost.setTitle(keyword);
    targetPost.setCreatedAt(LocalDateTime.now().minusDays(1));
    contentMatchedPost.setTitle("content-match-only-" + UUID.randomUUID());
    contentMatchedPost.setContent(keyword);
    contentMatchedPost.setCreatedAt(LocalDateTime.now());
    postRepository.saveAll(List.of(targetPost, contentMatchedPost));
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    MultiValueMap<String, String> params = TestParams.withPaging(0,
        boardDetailDataGenerator.getThemeBoards().size());
    params.add("keyword", keyword);
    // when
    List<ThemeBoardPreviewDto> response = requestThemeBoardPreviews(params, client);
    // then
    assertThat(response).hasSize(boardDetailDataGenerator.getThemeBoards().size());
    assertThat(response.get(0).getPostId()).isEqualTo(targetPost.getPostId());
    assertThat(response)
        .extracting(ThemeBoardPreviewDto::getPostId)
        .contains(contentMatchedPost.getPostId());
  }

  @Test
  @DisplayName("type_code 검색 조건에 매칭되는 테마 게시글 목록 정보를 최상단에 반환한다.")
  void findThemeBoards_typeCodeMatchedPostFirst() throws Exception {
    // given
    User client = boardDetailDataGenerator.getUsers().get(0);
    TypeCode typeCode = TypeCode.CHAT_ROOM_BACKGROUND_IMAGE;
    ComponentType componentType = getComponentType(typeCode);
    ThemeBoard targetThemeBoard = boardDetailDataGenerator.getThemeBoards().get(
        boardDetailDataGenerator.getThemeBoards().size() - 1);
    leaveTypeCodeOnTargetThemeOnly(componentType.getTypeCode(), targetThemeBoard);
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    MultiValueMap<String, String> params = TestParams.withPaging(0,
        boardDetailDataGenerator.getThemeBoards().size());
    params.add("type_code", componentType.getTypeCode().getTypeCode());
    // when
    List<ThemeBoardPreviewDto> response = requestThemeBoardPreviews(params, client);
    // then
    assertThat(response).hasSize(boardDetailDataGenerator.getThemeBoards().size());
    assertThat(response.get(0).getPostId()).isEqualTo(targetThemeBoard.getPost().getPostId());
    assertThat(response.get(0).getComponentTypes())
        .extracting(componentTypeDto -> componentTypeDto.getTypeCode().getTypeCode())
        .contains(typeCode.getTypeCode());
  }

  @Test
  @DisplayName("지원하지 않는 type_code 요청 시 400을 반환한다.")
  void findThemeBoards_invalidTypeCode() throws Exception {
    // given
    User client = boardDetailDataGenerator.getUsers().get(0);
    MultiValueMap<String, String> params = TestParams.withPaging(0, 5);
    params.add("type_code", "invalidTypeCode");
    // when & then
    mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, Void>builder()
            .mockMvc(mockMvc)
            .path("/api/theme-boards")
            .params(params)
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(400)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
  }

  @Test
  @DisplayName("테마 게시글 생성 성공 테스트")
  void createPost_success() throws Exception {
    // given
    String requestPath = "/api/theme-boards";
    User author = boardDetailDataGenerator.getUsers().get(0);
    List<String> tagNames = List.of("a", "b");
    List<TagCreateDto> postTags = tagNames.stream()
        .map(tagName -> TagCreateDto
            .builder()
            .tagName(tagName)
            .build())
        .toList();
    ThemeComponent nonThemeBoardTheme = boardDetailDataGenerator.getNonThemeBoardThemeComponents()
        .get(0);
    String testPreviewImageUrl = UUID.randomUUID().toString();
    ThemeBoardCreateDto createDto = ThemeBoardCreateDto.builder()
        .title(UUID.randomUUID().toString())
        .content(UUID.randomUUID().toString())
        .themeComponentId(nonThemeBoardTheme.getThemeComponentId())
        .publicFlag(true)
        .postTags(postTags)
        .build();
    MockMultipartFile testPreviewImage = MockMultipartFileUtils
        .generateImageFormData("preview_image", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("board_info", createDto);
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
    assertThat(response.getTags().stream().map(TagResponse::getTagName))
        .containsExactlyInAnyOrderElementsOf(tagNames);
    assertThemeBoardDetail(response);
  }

  @Test
  @DisplayName("테마 게시글 수정 성공 테스트")
  void updatePost_success() throws Exception {
    // given
    Post toUpdate = boardDetailDataGenerator.getPosts().get(0);
    String testPreviewImageUrl = UUID.randomUUID().toString();
    String requestPath = String.format("/api/theme-boards/%d", toUpdate.getPostId());
    List<String> tagNames = List.of("a", "b");
    List<TagUpdateDto> postTags = tagNames.stream()
        .map(tagName -> TagUpdateDto
            .builder()
            .tagName(tagName)
            .build())
        .toList();
    User author = toUpdate.getUser();
    ThemeBoardUpdateDto updateDto = ThemeBoardUpdateDto.builder()
        .title("updated-title-test")
        .postTags(postTags)
        .build();
    MockMultipartFile testPreviewImage = MockMultipartFileUtils
        .generateImageFormData("preview_image", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("board_info", updateDto);
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
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(author))
            .formDataList(formDataList)
            .responseType(new TypeReference<>() {
            })
            .statusCode(200)
            .build()
    );
    // then : 필드 검증
    assertThat(response.getTags().stream().map(TagResponse::getTagName))
        .containsExactlyInAnyOrderElementsOf(tagNames);
    assertThat(response.getPreviewImageUrl()).isEqualTo(List.of(testPreviewImageUrl));
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
