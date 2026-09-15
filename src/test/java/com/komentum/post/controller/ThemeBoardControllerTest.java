package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.enums.TypeCode;
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
import com.komentum.post.repository.PreferRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.test.data.MockMultipartFileUtils.ImageExtension;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport.DesignComponentScenarioResult;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport.ThemeComponentScenarioResult;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport.UserScenarioResult;
import com.komentum.test.dto.MockMvcMultipartRequestDto;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.dto.ThemeDesignAssetDto;
import com.komentum.user.domain.Follow;
import com.komentum.user.domain.User;
import com.komentum.user.repository.FollowRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class ThemeBoardControllerTest {

  @Autowired
  private PostRepository postRepository;

  @Autowired
  private PreferRepository preferRepository;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private PostScenarioSupport postScenarioSupport;

  @Autowired
  private UserScenarioSupport userScenarioSupport;

  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;

  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;

  @Autowired
  private TestDataRemover testDataRemover;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private FileManager fileManager;

  @Autowired
  private FollowRepository followRepository;

  private final int maxPreferPerPost = 3;
  private User testClient;
  private UserScenarioResult userScenarioResult;
  private PostScenarioSupport.Result postScenarioResult;
  private DesignComponentScenarioResult designComponentScenarioResult;
  private ThemeComponentScenarioResult themeComponentScenarioResult;

  void stubFileManager() {
    Mockito.when(fileManager.uploadFile(any(), any())).thenReturn(UUID.randomUUID().toString());
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    Mockito.when(fileManager.convertUrlToFileName(anyString()))
        .thenReturn(UUID.randomUUID().toString());
  }

  @BeforeEach
  void setUp() {
    // stub
    stubFileManager();
    // 사용자 4명
    userScenarioResult = userScenarioSupport.builder().withUsers(4).build();
    // 디자인 에셋 8개
    designComponentScenarioResult = designComponentScenarioSupport.builder(
            userScenarioResult.users())
        .withCountPerUser(2).build();
    // 테마 8개
    themeComponentScenarioResult = themeComponentScenarioSupport.builder(userScenarioResult.users(),
            designComponentScenarioResult.designComponents())
        .withCountPerUser(2).build();
    // 테마 게시글 8개
    postScenarioResult = postScenarioSupport.builder(userScenarioResult.users())
        .withThemeBoards(themeComponentScenarioResult.themeComponents())
        .withPrefersPerPost(maxPreferPerPost)
        .build();
    testClient = userScenarioResult.getFirstUser();
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
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

  static Stream<Arguments> themeBoardSortCases() {
    return Stream.of(
        Arguments.of("CREATED_ASC", List.of(0, 1, 2, 3, 4, 5, 6, 7)),
        Arguments.of("CREATED_DESC", List.of(7, 6, 5, 4, 3, 2, 1, 0)),
        Arguments.of("PREFER_ASC", List.of(0, 7, 6, 5, 4, 3, 2, 1)),
        Arguments.of("PREFER_DESC", List.of(7, 6, 5, 4, 3, 2, 1, 0))
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("themeBoardSortCases")
  @DisplayName("테마 게시글 목록을 생성 시간 또는 좋아요 수로 정렬한다.")
  void findThemeBoards_sort(String sortType, List<Integer> expectedIndexes) throws Exception {
    // given
    User client = testClient;
    List<Post> posts = postScenarioResult.posts();
    preferRepository.deleteAll(preferRepository.fetchJoinByPostIn(List.of(posts.get(0))));
    LocalDateTime baseTime = LocalDateTime.of(2025, 1, 1, 0, 0);
    for (int i = 0; i < posts.size(); i++) {
      posts.get(i).setCreatedAt(baseTime.plusDays(i));
    }
    postRepository.saveAll(posts);
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    MultiValueMap<String, String> params = TestParams.withPaging(0,
        postScenarioResult.themeBoards().size());
    params.add("sort_type", sortType);
    List<Long> expectedPostIds = expectedIndexes.stream()
        .map(index -> postScenarioResult.themeBoards().get(index).getPost().getPostId())
        .toList();
    // when
    List<ThemeBoardPreviewDto> response = requestThemeBoardPreviews(params, client);
    // then
    assertThat(response)
        .extracting(ThemeBoardPreviewDto::getPostId)
        .containsExactlyElementsOf(expectedPostIds);
  }

  @Test
  @DisplayName("테마 게시글 정렬값을 생략하면 생성 시간 내림차순으로 정렬한다.")
  void findThemeBoards_defaultSort() throws Exception {
    // given
    User client = testClient;
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    MultiValueMap<String, String> defaultParams = TestParams.withPaging(0, 5);
    MultiValueMap<String, String> blankParams = TestParams.withPaging(0, 5);
    blankParams.add("sort_type", "");
    MultiValueMap<String, String> createdDescParams = TestParams.withPaging(0, 5);
    createdDescParams.add("sort_type", "CREATED_DESC");
    // when
    List<ThemeBoardPreviewDto> defaultResponse = requestThemeBoardPreviews(defaultParams, client);
    List<ThemeBoardPreviewDto> blankResponse = requestThemeBoardPreviews(blankParams, client);
    List<ThemeBoardPreviewDto> createdDescResponse = requestThemeBoardPreviews(createdDescParams,
        client);
    // then
    List<Long> expectedPostIds = createdDescResponse.stream()
        .map(ThemeBoardPreviewDto::getPostId)
        .toList();
    assertThat(defaultResponse)
        .extracting(ThemeBoardPreviewDto::getPostId)
        .containsExactlyElementsOf(expectedPostIds);
    assertThat(blankResponse)
        .extracting(ThemeBoardPreviewDto::getPostId)
        .containsExactlyElementsOf(expectedPostIds);
  }

  @ParameterizedTest
  @ValueSource(strings = {"DEFAULT", "created_desc"})
  @DisplayName("지원하지 않는 테마 게시글 정렬값은 400을 반환한다.")
  void findThemeBoards_invalidSort(String sortType) throws Exception {
    // given
    User client = testClient;
    MultiValueMap<String, String> params = TestParams.withPaging(0, 5);
    params.add("sort_type", sortType);
    // when & then
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/theme-boards").params(params), TestClientDto.fromEntity(client)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").isNotEmpty());
  }

  @Test
  @DisplayName("페이지 기반 테마 게시글 목록 조회 성공 테스트")
  void getPosts_success() throws Exception {
    // given
    int pageNumber = 0;
    int pageSize = 3;
    String requestPath = "/api/theme-boards";
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
            .clientDto(TestClientDto.fromEntity(testClient))
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
    int pageSize = 3;
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
            .clientDto(TestClientDto.fromEntity(testClient))
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
    int pageSize = 3;
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
            .clientDto(TestClientDto.fromEntity(testClient))
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
  @DisplayName("테마 게시글 상세 조회 : withImages=true이면 테마 이미지 함께 반환")
  void findThemeBoardByPostId_withImagesTrue() throws Exception {
    // given
    ThemeBoard targetThemeBoard = postScenarioResult.themeBoards().get(0);
    Long postId = targetThemeBoard.getPost().getPostId();
    MultiValueMap<String, String> params = TestParams.withEmpty();
    params.add("withImages", "true");
    // stub
    stubFileManager();
    // when
    ThemeBoardDetailDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, ThemeBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/theme-boards/%d", postId))
            .params(params)
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(testClient))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThemeBoardDetail(response);
    assertThat(response.getThemeDesignAssetDtoList()).hasSize(TypeCode.values().length);
    for (ThemeDesignAssetDto dto : response.getThemeDesignAssetDtoList()) {
      assertThat(dto.getDesignComponentId()).isNotNull();
      assertThat(dto.getImageUrl()).isNotNull();
      assertThat(dto.getTypeCode()).isNotNull();
      assertThat(dto.getTypeCodeGroup()).isNotNull();
      assertThat(dto.getTypeCodeGroupName()).isNotBlank();
    }
  }

  @Test
  @DisplayName("테마 게시글 상세 조회 : withImages=null이면 테마 게시글을 반환하지 않음")
  void findThemeBoardByPostId_withImagesFalse() throws Exception {
    // given
    ThemeBoard targetThemeBoard = postScenarioResult.themeBoards().get(0);
    Long postId = targetThemeBoard.getPost().getPostId();
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    ThemeBoardDetailDto response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, ThemeBoardDetailDto>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/theme-boards/%d", postId))
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(testClient))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    assertThemeBoardDetail(response);
    assertThat(response.getThemeDesignAssetDtoList()).isNull();
  }

  /**
   * 인증 정보 없이 테마 게시글 단건 상세를 조회할 수 있는지 검증한다.
   */
  @Test
  @DisplayName("비인증 사용자는 테마 게시글 단건 상세를 조회할 수 있다")
  void findThemeBoardByPostId_withoutAuthentication_returnsDetail() throws Exception {
    // given
    ThemeBoard targetThemeBoard = postScenarioResult.themeBoards().get(0);
    Post targetPost = targetThemeBoard.getPost();
    // when
    ResultActions result = mockMvc.perform(
            get("/api/theme-boards/{postId}", targetPost.getPostId()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.liked").exists())
        .andExpect(jsonPath("$.liked").value(false))
        .andExpect(jsonPath("$.following").exists())
        .andExpect(jsonPath("$.following").value(false));
    ThemeBoardDetailDto response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    // then
    assertThemeBoardDetail(response);
    assertThat(response.getPostId()).isEqualTo(targetPost.getPostId());
    assertThat(response.getTitle()).isEqualTo(targetPost.getTitle());
    assertThat(response.getContent()).isEqualTo(targetPost.getContent());
  }

  @Test
  @DisplayName("테마 게시글 단건 상세 조회 시 현재 사용자의 작성자 팔로우 여부를 반환한다")
  void findThemeBoardByPostId_returnsFollowingStatus() throws Exception {
    // given
    ThemeBoard targetThemeBoard = postScenarioResult.themeBoards().get(0);
    User author = targetThemeBoard.getPost().getUser();
    User followingClient = userScenarioResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .findFirst()
        .orElseThrow();
    User nonFollowingClient = userScenarioResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .filter(user -> !user.getUserId().equals(followingClient.getUserId()))
        .findFirst()
        .orElseThrow();
    followRepository.saveAndFlush(new Follow(followingClient, author));
    // when & then
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/theme-boards/{postId}", targetThemeBoard.getPost().getPostId()),
            TestClientDto.fromEntity(followingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.following").exists())
        .andExpect(jsonPath("$.following").value(true));
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/theme-boards/{postId}", targetThemeBoard.getPost().getPostId()),
            TestClientDto.fromEntity(nonFollowingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.following").exists())
        .andExpect(jsonPath("$.following").value(false));
  }

  @Test
  @DisplayName("If a pinned post ID is provided, place that post at the top of the first page and return only theme boards written by the same author.")
  void findThemeBoardDetails_ifPinnedPostIdExists() throws Exception {
    // given
    Post pinnedPost = postScenarioResult.posts().get(0);
    User pinnedAuthor = pinnedPost.getUser();
    MultiValueMap<String, String> params = TestParams.withPaging(0, 10);
    params.add("pinnedPostId", pinnedPost.getPostId().toString());
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardDetailDto> response = requestThemeBoardDetails(params, testClient);
    // then
    assertThat(response).isNotEmpty();
    assertThat(response.get(0).getPostId()).isEqualTo(pinnedPost.getPostId());
    for (ThemeBoardDetailDto dto : response) {
      assertThemeBoardDetail(dto);
      assertThat(dto.getUserEmail()).isEqualTo(pinnedAuthor.getUserEmail());
    }
  }

  @Test
  @DisplayName("When no pinned post ID is provided, return theme board details using requested paging.")
  void findThemeBoardDetails_withoutPinnedPostId_returnsRequestedPage() throws Exception {
    // given
    int pageSize = 3;
    MultiValueMap<String, String> params = TestParams.withPaging(0, pageSize);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardDetailDto> response = requestThemeBoardDetails(params, testClient);
    // then
    assertThat(response).hasSize(pageSize);
    for (ThemeBoardDetailDto dto : response) {
      assertThemeBoardDetail(dto);
    }
  }

  /**
   * 인증 정보 없이 기본 페이징 조건으로 테마 게시글 상세 목록을 조회할 수 있는지 검증한다.
   */
  @Test
  @DisplayName("비인증 사용자는 기본 테마 게시글 상세 목록을 조회할 수 있다")
  void findThemeBoardDetails_withoutAuthentication_returnsDefaultPage() throws Exception {
    // given
    int pageSize = postScenarioResult.themeBoards().size();
    List<Long> expectedPostIds = postScenarioResult.themeBoards().stream()
        .map(themeBoard -> themeBoard.getPost().getPostId())
        .toList();
    // when
    ResultActions result = mockMvc.perform(
            get("/api/theme-boards/details").params(TestParams.withPaging(0, pageSize)))
        .andExpect(status().isOk());
    List<ThemeBoardDetailDto> response = mockMvcUtils.parseResponse(result,
        new TypeReference<>() {
        });
    // then
    assertThat(response)
        .isNotEmpty()
        .hasSize(pageSize)
        .extracting(ThemeBoardDetailDto::getPostId)
        .containsExactlyInAnyOrderElementsOf(expectedPostIds);
    for (int i = 0; i < response.size(); i++) {
      assertThemeBoardDetail(response.get(i));
      result
          .andExpect(jsonPath("$[" + i + "].liked").exists())
          .andExpect(jsonPath("$[" + i + "].liked").value(false))
          .andExpect(jsonPath("$[" + i + "].following").exists())
          .andExpect(jsonPath("$[" + i + "].following").value(false));
    }
  }

  @Test
  @DisplayName("테마 게시글 상세 목록 조회 시 현재 사용자의 작성자 팔로우 여부를 반환한다")
  void findThemeBoardDetails_returnsFollowingStatus() throws Exception {
    // given
    ThemeBoard targetThemeBoard = postScenarioResult.themeBoards().get(0);
    Post targetPost = targetThemeBoard.getPost();
    User author = targetPost.getUser();
    User followingClient = userScenarioResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .findFirst()
        .orElseThrow();
    User nonFollowingClient = userScenarioResult.users().stream()
        .filter(user -> !user.getUserId().equals(author.getUserId()))
        .filter(user -> !user.getUserId().equals(followingClient.getUserId()))
        .findFirst()
        .orElseThrow();
    followRepository.saveAndFlush(new Follow(followingClient, author));
    MultiValueMap<String, String> params = TestParams.withPaging(0, 1);
    params.add("pinnedPostId", targetPost.getPostId().toString());
    // when & then
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/theme-boards/details").params(params),
            TestClientDto.fromEntity(followingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].following").exists())
        .andExpect(jsonPath("$[0].following").value(true));
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/theme-boards/details").params(params),
            TestClientDto.fromEntity(nonFollowingClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].following").exists())
        .andExpect(jsonPath("$[0].following").value(false));
  }

  @Test
  @DisplayName("When requesting a later pinned page, do not return the pinned post again.")
  void findThemeBoardDetails_withPinnedPostIdOnLaterPage_doesNotReturnPinnedPostAgain()
      throws Exception {
    // given
    Post pinnedPost = postScenarioResult.posts().get(0);
    MultiValueMap<String, String> params = TestParams.withPaging(1, 1);
    params.add("pinnedPostId", pinnedPost.getPostId().toString());
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardDetailDto> response = requestThemeBoardDetails(params, testClient);
    // then
    assertThat(response).isNotEmpty();
    assertThat(response)
        .extracting(ThemeBoardDetailDto::getPostId)
        .doesNotContain(pinnedPost.getPostId());
  }

  @Test
  @DisplayName("When withImages is true, include theme design assets in each detail.")
  void findThemeBoardDetails_withImagesTrue_returnsThemeDesignAssets() throws Exception {
    // given
    int pageSize = 2;
    MultiValueMap<String, String> params = TestParams.withPaging(0, pageSize);
    params.add("withImages", "true");
    // stub
    stubFileManager();
    // when
    List<ThemeBoardDetailDto> response = requestThemeBoardDetails(params, testClient);
    // then
    assertThat(response).hasSize(pageSize);
    for (ThemeBoardDetailDto dto : response) {
      assertThemeBoardDetail(dto);
      assertThat(dto.getThemeDesignAssetDtoList()).hasSize(TypeCode.values().length);
    }
  }

  @Test
  @DisplayName("When withImages is omitted, do not include theme design assets in details.")
  void findThemeBoardDetails_withoutWithImages_omitsThemeDesignAssets() throws Exception {
    // given
    int pageSize = 2;
    MultiValueMap<String, String> params = TestParams.withPaging(0, pageSize);
    // stub
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when
    List<ThemeBoardDetailDto> response = requestThemeBoardDetails(params, testClient);
    // then
    assertThat(response).hasSize(pageSize);
    for (ThemeBoardDetailDto dto : response) {
      assertThemeBoardDetail(dto);
      assertThat(dto.getThemeDesignAssetDtoList()).isNull();
    }
  }

  @Test
  @DisplayName("keyword 검색 조건에 매칭되는 테마 게시글 목록 정보를 최상단에 반환한다.")
  void findThemeBoards_keywordMatchedPostFirst() throws Exception {
    // given
    ThemeBoard targetThemeBoard = postScenarioResult.themeBoards().get(0);
    ThemeBoard contentMatchedThemeBoard = postScenarioResult.themeBoards()
        .get(postScenarioResult.themeBoards().size() - 1);
    Post targetPost = targetThemeBoard.getPost();
    Post contentMatchedPost = contentMatchedThemeBoard.getPost();
    String keyword = "theme-keyword-" + UUID.randomUUID();
    targetPost.setTitle(keyword);
    contentMatchedPost.setTitle("content-match-only-" + UUID.randomUUID());
    contentMatchedPost.setContent(keyword);
    postRepository.saveAll(List.of(targetPost, contentMatchedPost));
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    MultiValueMap<String, String> firstPageParams = TestParams.withPaging(0, 1);
    firstPageParams.add("keyword", keyword);
    firstPageParams.add("sort_type", "PREFER_ASC");
    MultiValueMap<String, String> secondPageParams = TestParams.withPaging(1, 1);
    secondPageParams.add("keyword", keyword);
    secondPageParams.add("sort_type", "PREFER_ASC");
    // when
    List<ThemeBoardPreviewDto> firstPage = requestThemeBoardPreviews(firstPageParams, testClient);
    List<ThemeBoardPreviewDto> secondPage = requestThemeBoardPreviews(secondPageParams, testClient);
    // then
    assertThat(firstPage)
        .extracting(ThemeBoardPreviewDto::getPostId)
        .containsExactly(targetPost.getPostId());
    assertThat(secondPage)
        .extracting(ThemeBoardPreviewDto::getPostId)
        .containsExactly(contentMatchedPost.getPostId());
  }

  @Test
  @DisplayName("테마 게시글 목록 응답에 component_types를 포함하지 않는다.")
  void findThemeBoards_excludesComponentTypes() throws Exception {
    // given
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    MultiValueMap<String, String> params = TestParams.withPaging(0, 5);
    // when & then
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/theme-boards").params(params), TestClientDto.fromEntity(testClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].componentTypes").doesNotExist());
  }

  @Test
  @DisplayName("테마 게시글 상세 응답에 component_types를 포함하지 않는다.")
  void findThemeBoardByPostId_excludesComponentTypes() throws Exception {
    // given
    ThemeBoard targetThemeBoard = postScenarioResult.themeBoards().get(0);
    Mockito.when(fileManager.resolveFilePath(anyString()))
        .thenReturn(UUID.randomUUID().toString());
    // when & then
    mockMvc.perform(mockMvcUtils.addAuthentication(
            get("/api/theme-boards/{postId}", targetThemeBoard.getPost().getPostId()),
            TestClientDto.fromEntity(testClient)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.componentTypes").doesNotExist());
  }

  @Test
  @DisplayName("테마 게시글 생성 성공 테스트")
  void createPost_success() throws Exception {
    // given
    String requestPath = "/api/theme-boards";
    User author = testClient;
    List<String> tagNames = List.of("a", "b");
    List<TagCreateDto> postTags = tagNames.stream()
        .map(tagName -> TagCreateDto
            .builder()
            .tagName(tagName)
            .build())
        .toList();
    List<ThemeComponent> nonBoardThemes = themeComponentScenarioSupport.builder(
            userScenarioResult.users(), designComponentScenarioResult.designComponents())
        .withCountPerUser(1)
        .build()
        .themeComponents();
    ThemeComponent targetTheme = nonBoardThemes.get(0);
    String testPreviewImageUrl = UUID.randomUUID().toString();
    String expectedTitle = "t".repeat(100);
    String expectedContent = "c".repeat(2000);
    ThemeBoardCreateDto createDto = ThemeBoardCreateDto.builder()
        .title(expectedTitle)
        .content(expectedContent)
        .themeComponentId(targetTheme.getThemeComponentId())
        .publicFlag(true)
        .postTags(postTags)
        .build();
    MockMultipartFile testPreviewImage = MockMultipartFileUtils
        .generateImageFormData("previewImage", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", createDto);
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
    assertThat(response.getTitle()).isEqualTo(expectedTitle);
    assertThat(response.getContent()).isEqualTo(expectedContent);
    Post savedPost = postRepository.findById(response.getPostId()).orElseThrow();
    assertThat(savedPost.getTitle()).isEqualTo(expectedTitle);
    assertThat(savedPost.getContent()).isEqualTo(expectedContent);
    assertThemeBoardDetail(response);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"title", "content"})
  @DisplayName("테마 게시글 생성 시 제목 또는 본문이 최대 길이를 초과하면 400을 반환한다")
  void createPost_exceedingTextLimit_returnsBadRequest(String invalidField) throws Exception {
    // given
    ThemeComponent targetTheme = themeComponentScenarioSupport.builder(
            userScenarioResult.users(), designComponentScenarioResult.designComponents())
        .withCountPerUser(1)
        .build()
        .themeComponents()
        .get(0);
    ThemeBoardCreateDto createDto = ThemeBoardCreateDto.builder()
        .title("title".equals(invalidField) ? "t".repeat(101) : "t".repeat(100))
        .content("content".equals(invalidField) ? "c".repeat(2001) : "c".repeat(2000))
        .themeComponentId(targetTheme.getThemeComponentId())
        .publicFlag(true)
        .postTags(List.of())
        .build();
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", createDto);
    // when & then
    Map<String, String> response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<Map<String, String>>builder()
            .mockMvc(mockMvc)
            .path("/api/theme-boards")
            .httpMethod(HttpMethod.POST)
            .clientDto(TestClientDto.fromEntity(testClient))
            .formDataList(List.of(boardInfo))
            .statusCode(400)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    assertThat(response).containsKey(invalidField);
  }

  @Test
  @DisplayName("테마 게시글 수정 성공 테스트")
  void updatePost_success() throws Exception {
    // given
    Post toUpdate = postScenarioResult.posts().get(0);
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
    String expectedTitle = "t".repeat(100);
    String expectedContent = "c".repeat(2000);
    ThemeBoardUpdateDto updateDto = ThemeBoardUpdateDto.builder()
        .title(expectedTitle)
        .content(expectedContent)
        .postTags(postTags)
        .build();
    MockMultipartFile testPreviewImage = MockMultipartFileUtils
        .generateImageFormData("previewImage", ImageExtension.PNG);
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", updateDto);
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
    assertThat(response.getTitle()).isEqualTo(expectedTitle);
    assertThat(response.getContent()).isEqualTo(expectedContent);
    Post savedPost = postRepository.findById(response.getPostId()).orElseThrow();
    assertThat(savedPost.getTitle()).isEqualTo(expectedTitle);
    assertThat(savedPost.getContent()).isEqualTo(expectedContent);
    assertThemeBoardDetail(response);
  }

  @ParameterizedTest(name = "{0}")
  @ValueSource(strings = {"title", "content"})
  @DisplayName("테마 게시글 수정 시 제목 또는 본문이 최대 길이를 초과하면 400을 반환한다")
  void updatePost_exceedingTextLimit_returnsBadRequest(String invalidField) throws Exception {
    // given
    Post toUpdate = postScenarioResult.posts().get(0);
    ThemeBoardUpdateDto updateDto = ThemeBoardUpdateDto.builder()
        .title("title".equals(invalidField) ? "t".repeat(101) : "t".repeat(100))
        .content("content".equals(invalidField) ? "c".repeat(2001) : "c".repeat(2000))
        .postTags(List.of())
        .build();
    MockMultipartFile boardInfo = MockMultipartFileUtils
        .generateJsonFormData("boardInfo", updateDto);
    // when & then
    Map<String, String> response = mockMvcUtils.doAuthMultipartRequest(
        MockMvcMultipartRequestDto.<Map<String, String>>builder()
            .mockMvc(mockMvc)
            .path(String.format("/api/theme-boards/%d", toUpdate.getPostId()))
            .httpMethod(HttpMethod.PUT)
            .clientDto(TestClientDto.fromEntity(toUpdate.getUser()))
            .formDataList(List.of(boardInfo))
            .statusCode(400)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    assertThat(response).containsKey(invalidField);
  }

  @Test
  @DisplayName("테마 게시글 삭제 성공 테스트")
  void deletePost_success() throws Exception {
    // given
    Post toDelete = postScenarioResult.posts().get(0);
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
