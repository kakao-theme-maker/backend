package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.ComponentTypeRepository;
import com.komentum.designcomponent.repository.DesignComponentRepository;
import com.komentum.global.utils.DateUtils;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Category;
import com.komentum.post.domain.CategoryPost;
import com.komentum.post.domain.DesignBoard;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.repository.CategoryPostRepository;
import com.komentum.post.repository.CategoryRepository;
import com.komentum.post.service.enums.CategoryType;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@EnableTestProfile
@AutoConfigureMockMvc
@SpringBootTest
public class UserPostControllerTest {

  private static final String PREVIEW_IMAGE_URL = "http://mocked-url/user-post-preview.png";
  private static final String PREVIEW_IMAGE_NAME = "user-post-preview.png";
  private static final String PUBLIC_UPLOAD_POSTS_PATH = "/api/users/{publicUserId}/upload-posts";
  private static final List<String> USER_POST_PATHS = List.of(
      "/api/users/me/upload-posts",
      "/api/users/me/bookmarked-posts",
      "/api/users/me/preferred-posts");

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private FileManager fileManager;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private TestDataRemover testDataRemover;

  @Autowired
  private UserScenarioSupport userScenarioSupport;

  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;

  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;

  @Autowired
  private PostScenarioSupport postScenarioSupport;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private CategoryPostRepository categoryPostRepository;

  @Autowired
  private DesignComponentRepository designComponentRepository;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  private User client;
  private User otherAuthor;
  private Post customCategoryOnlyPost;
  private PostScenarioSupport.Result postResult;
  private Map<Long, Post> postById;
  private Set<Long> bookmarkedPostIds;
  private Set<Long> preferredPostIds;

  @BeforeEach
  void setUp() {
    given(fileManager.resolvePublicFileUrl(any())).willReturn(PREVIEW_IMAGE_URL);
    given(fileManager.convertUrlToFileName(any())).willReturn(PREVIEW_IMAGE_NAME);
    componentTypeRepository.save(ComponentType.builder()
        .name("theme-icon")
        .typeCode(TypeCode.COMMON_ICO_THEME)
        .build());

    List<User> users = userScenarioSupport.builder()
        .withUsers(3)
        .build()
        .users();
    client = users.get(0);
    otherAuthor = users.get(1);

    List<DesignComponent> designComponents = designComponentScenarioSupport.builder(users)
        .withCountPerUser(2)
        .build()
        .designComponents();
    List<ThemeComponent> themeComponents = themeComponentScenarioSupport.builder(users,
            designComponents)
        .withCountPerUser(2)
        .build()
        .themeComponents();
    Map<Long, List<DesignComponent>> designComponentsByUserId = designComponents.stream()
        .collect(Collectors.groupingBy(dc -> dc.getUser().getUserId()));
    Map<User, List<DesignComponent>> designComponentOwnerMap = users.stream()
        .collect(Collectors.toMap(Function.identity(),
            user -> designComponentsByUserId.get(user.getUserId())));

    postResult = postScenarioSupport.builder(users)
        .withThemeBoards(themeComponents)
        .withDesignBoardsPerUser(1, designComponentOwnerMap)
        .withPrefersPerPost(1)
        .build();
    postById = postResult.posts().stream()
        .collect(Collectors.toMap(Post::getPostId, Function.identity()));
    preferredPostIds = resolvePreferredPostIds();
    bookmarkedPostIds = createBookmarkAndCustomCategory();
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("내가 작성한 게시글 목록은 post_type으로 필터링한다")
  void findUserPostList_filtersByPostType() throws Exception {
    assertEndpointFilters(
        "/api/users/me/upload-posts",
        expectedUploadedPostIds(null),
        expectedUploadedPostIds(PostType.THEME_BOARD),
        expectedUploadedPostIds(PostType.DESIGN_BOARD)
    );
  }

  /** 지정한 작성자의 게시글 조회와 테마 게시글의 최신순 페이지 이동을 검증한다. */
  @Test
  @DisplayName("다른 사용자의 게시글을 조회하고 테마 게시글만 최신순으로 페이지 조회한다")
  void publicUserPostList_filtersAndPaginatesOtherAuthorsPosts() throws Exception {
    String path = publicUploadPostsPath(otherAuthor);
    List<Long> allPosts = expectedUploadedPostIds(otherAuthor, null);
    assertUserPostListResponse(readUserPosts(performGet(path + "?page=0", null, 2)),
        allPosts.subList(0, 2));

    List<Long> themePosts = expectedUploadedPostIds(otherAuthor, PostType.THEME_BOARD);
    assertThat(readUserPosts(performGet(path + "?page=1", PostType.THEME_BOARD, 1)))
        .extracting(UserPostListResponseDto::getPostId)
        .containsExactly(themePosts.get(1));
  }

  /** 좋아요와 북마크가 인증된 조회자에게만 반영되는지 검증한다. */
  @Test
  @DisplayName("공개 목록에서 좋아요와 북마크는 조회자 기준이며 비로그인 요청에는 false다")
  void publicUserPostList_usesViewerForInteractionFlags() throws Exception {
    Long postId = expectedUploadedPostIds(otherAuthor, PostType.DESIGN_BOARD).get(0);
    Category bookmark = categoryRepository.findByCategoryTypeAndOwner(CategoryType.BOOKMARK, client)
        .orElseThrow();
    categoryPostRepository.save(CategoryPost.builder()
        .category(bookmark)
        .post(postById.get(postId))
        .build());

    String path = publicUploadPostsPath(otherAuthor);
    MockHttpServletRequestBuilder authenticatedRequest = get(path)
        .queryParam("postType", PostType.DESIGN_BOARD.name());
    mockMvc.perform(mockMvcUtils.addAuthentication(authenticatedRequest,
            TestClientDto.fromEntity(client)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].postId").value(postId))
        .andExpect(jsonPath("$[0].preferred").value(true))
        .andExpect(jsonPath("$[0].bookmarked").value(true));

    mockMvc.perform(get(path).queryParam("postType", PostType.DESIGN_BOARD.name()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].postId").value(postId))
        .andExpect(jsonPath("$[0].preferred").value(false))
        .andExpect(jsonPath("$[0].bookmarked").value(false));

    mockMvc.perform(get("/api/users/me/upload-posts").queryParam("size", "1"))
        .andExpect(status().isUnauthorized());
  }

  /** 작성한 게시글이 없는 사용자와 등록되지 않은 사용자를 구분하는지 검증한다. */
  @Test
  @DisplayName("게시글이 없는 사용자는 빈 배열을 반환하고 등록되지 않은 사용자는 404를 반환한다")
  void publicUserPostList_distinguishesEmptyAndMissingAuthor() throws Exception {
    User emptyAuthor = userScenarioSupport.builder().withUsers(1).build().getFirstUser();

    mockMvc.perform(get(publicUploadPostsPath(emptyAuthor)))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
    mockMvc.perform(get(PUBLIC_UPLOAD_POSTS_PATH, UUID.randomUUID()))
        .andExpect(status().isNotFound());
  }

  /** 잘못된 사용자 ID와 게시글 종류를 거부하는지 검증한다. */
  @Test
  @DisplayName("잘못된 공개 ID 또는 postType으로 조회하면 400을 반환한다")
  void publicUserPostList_rejectsInvalidFilters() throws Exception {
    mockMvc.perform(get(PUBLIC_UPLOAD_POSTS_PATH, "not-a-uuid"))
        .andExpect(status().isBadRequest());
    mockMvc.perform(get(publicUploadPostsPath(otherAuthor)).queryParam("postType", "INVALID"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("북마크한 게시글 목록은 post_type으로 필터링한다")
  void findBookmarkedPostList_filtersByPostType() throws Exception {
    assertEndpointFilters(
        "/api/users/me/bookmarked-posts",
        expectedBookmarkedPostIds(null),
        expectedBookmarkedPostIds(PostType.THEME_BOARD),
        expectedBookmarkedPostIds(PostType.DESIGN_BOARD)
    );
  }

  @Test
  @DisplayName("좋아요한 게시글 목록은 post_type으로 필터링한다")
  void findPreferredPostList_filtersByPostType() throws Exception {
    assertEndpointFilters(
        "/api/users/me/preferred-posts",
        expectedPreferredPostIds(null),
        expectedPreferredPostIds(PostType.THEME_BOARD),
        expectedPreferredPostIds(PostType.DESIGN_BOARD)
    );
  }

  @Test
  @DisplayName("사용자 게시글 목록 응답은 camelCase 필드를 사용한다")
  void userPostListResponse_usesCamelCaseFields() throws Exception {
    MvcResult result = performGet("/api/users/me/upload-posts", null);
    JsonNode first = objectMapper.readTree(result.getResponse().getContentAsString()).get(0);

    assertThat(first.has("postId")).isTrue();
    assertThat(first.has("postType")).isTrue();
    assertThat(first.has("componentId")).isFalse();
    assertThat(first.has("previewImageUrl")).isTrue();
    assertThat(first.has("createdAt")).isTrue();
    assertThat(first.has("updatedAt")).isTrue();
    assertThat(first.has("authorName")).isTrue();
    assertThat(first.has("authorProfileImageUrl")).isTrue();
    assertThat(first.has("preferred")).isTrue();
    assertThat(first.has("bookmarked")).isTrue();
    assertThat(first.get("createdAt").asText()).matches("\\d{4}-\\d{2}-\\d{2}");
    assertThat(first.get("updatedAt").asText()).matches("\\d{4}-\\d{2}-\\d{2}");

    assertThat(first.has("post_id")).isFalse();
    assertThat(first.has("post_type")).isFalse();
    assertThat(first.has("component_id")).isFalse();
    assertThat(first.has("preview_image_url")).isFalse();
    assertThat(first.has("created_at")).isFalse();
    assertThat(first.has("updated_at")).isFalse();
    assertThat(first.has("user_name")).isFalse();
    assertThat(first.has("profile_image")).isFalse();
    assertThat(first.has("userName")).isFalse();
    assertThat(first.has("profileImage")).isFalse();
    assertThat(first.has("liked")).isFalse();
  }

  @Test
  @DisplayName("커스텀 카테고리에만 담긴 게시글은 북마크로 표시하지 않는다")
  void customCategoryPost_isNotMarkedAsBookmarked() throws Exception {
    List<UserPostListResponseDto> response = requestUserPosts("/api/users/me/upload-posts", null);

    UserPostListResponseDto customCategoryPostResponse = response.stream()
        .filter(dto -> dto.getPostId().equals(customCategoryOnlyPost.getPostId()))
        .findFirst()
        .orElseThrow();

    assertThat(customCategoryPostResponse.isBookmarked()).isFalse();
  }

  @Test
  @DisplayName("일부 컴포넌트가 삭제된 디자인 게시글은 사용자 목록에서 활성 에셋을 대표 이미지로 표시한다")
  void userPostLists_partiallyInactiveDesignPostUsesActivePreview() throws Exception {
    List<DesignBoard> boards = postResult.designBoards().stream()
        .filter(board -> board.getPost().getUser().getUserId().equals(client.getUserId()))
        .sorted(Comparator.comparing(board -> board.getDesignComponent().getDesignComponentId()))
        .toList();
    DesignComponent deletedComponent = boards.get(0).getDesignComponent();
    DesignComponent activeComponent = boards.get(1).getDesignComponent();
    deletedComponent.deactivate();
    designComponentRepository.saveAndFlush(deletedComponent);
    given(fileManager.resolvePublicFileUrl(any()))
        .willAnswer(invocation -> "http://mocked-url/" + invocation.getArgument(0, String.class));

    for (String path : USER_POST_PATHS) {
      UserPostListResponseDto response = requestUserPosts(path, PostType.DESIGN_BOARD).stream()
          .filter(dto -> dto.getPostId().equals(boards.get(0).getPost().getPostId()))
          .findFirst()
          .orElseThrow();
      assertThat(response.getPreviewImageUrl())
          .containsExactly("http://mocked-url/" + activeComponent.getFileName());
    }
  }

  @Test
  @DisplayName("전체 컴포넌트가 삭제된 디자인 게시글은 페이징 전에 숨기고 테마 게시글은 유지한다")
  void userPostLists_inactiveDesignPostsAreHiddenBeforePagination() throws Exception {
    List<DesignComponent> components = designComponentRepository.findAll();
    components.forEach(DesignComponent::deactivate);
    designComponentRepository.saveAllAndFlush(components);

    for (String path : USER_POST_PATHS) {
      assertThat(requestUserPosts(path, PostType.DESIGN_BOARD)).isEmpty();
      List<Long> expectedThemePostIds = switch (path) {
        case "/api/users/me/upload-posts" -> expectedUploadedPostIds(PostType.THEME_BOARD);
        case "/api/users/me/bookmarked-posts" -> expectedBookmarkedPostIds(PostType.THEME_BOARD);
        default -> expectedPreferredPostIds(PostType.THEME_BOARD);
      };
      assertUserPostListResponse(requestUserPosts(path, null), expectedThemePostIds);
      List<UserPostListResponseDto> firstPage = readUserPosts(performGet(path, null, 1));
      assertThat(firstPage).extracting(UserPostListResponseDto::getPostId)
          .containsExactly(expectedThemePostIds.get(0));
    }

    List<UserPostListResponseDto> publicFirstPage = readUserPosts(
        performGet(publicUploadPostsPath(client), null, 1));
    assertThat(publicFirstPage).extracting(UserPostListResponseDto::getPostId)
        .containsExactly(expectedUploadedPostIds(PostType.THEME_BOARD).get(0));
  }

  /** 작성자의 공개 ID가 포함된 게시글 목록 경로를 반환한다. */
  private String publicUploadPostsPath(User author) {
    return PUBLIC_UPLOAD_POSTS_PATH.replace("{publicUserId}", author.getPublicUserId());
  }

  private void assertEndpointFilters(String path, List<Long> allPostIds, List<Long> themePostIds,
      List<Long> designPostIds) throws Exception {
    assertUserPostListResponse(requestUserPosts(path, null), allPostIds);
    assertUserPostListResponse(requestUserPosts(path, PostType.THEME_BOARD), themePostIds);
    assertUserPostListResponse(requestUserPosts(path, PostType.DESIGN_BOARD), designPostIds);
  }

  private void assertUserPostListResponse(List<UserPostListResponseDto> response,
      List<Long> expectedPostIds) {
    assertThat(response)
        .extracting(UserPostListResponseDto::getPostId)
        .containsExactlyElementsOf(expectedPostIds);

    response.forEach(dto -> {
      Post post = postById.get(dto.getPostId());
      assertThat(post).isNotNull();
      assertThat(dto.getPostType()).isEqualTo(post.getPostType());
      assertThat(dto.getTitle()).isEqualTo(post.getTitle());
      assertThat(dto.getContent()).isEqualTo(post.getContent());
      assertThat(dto.getAuthorName()).isEqualTo(post.getUser().getName());
      assertThat(dto.getAuthorProfileImageUrl()).isEqualTo(post.getUser().getProfileImgUrl());
      assertThat(dto.getCreatedAt()).isEqualTo(DateUtils.convertToDateString(post.getCreatedAt()));
      assertThat(dto.getUpdatedAt()).isEqualTo(DateUtils.convertToDateString(post.getUpdatedAt()));
      assertThat(dto.getPreviewImageUrl()).containsExactly(PREVIEW_IMAGE_URL);
      assertThat(dto.getTags()).isNotNull();
      assertThat(dto.getPrefers()).isEqualTo(1L);
      assertThat(dto.getComments()).isNotNull();
      assertThat(dto.isPreferred()).isEqualTo(preferredPostIds.contains(dto.getPostId()));
      assertThat(dto.isBookmarked()).isEqualTo(bookmarkedPostIds.contains(dto.getPostId()));
    });
  }

  private List<UserPostListResponseDto> requestUserPosts(String path, PostType postType)
      throws Exception {
    return readUserPosts(performGet(path, postType));
  }

  /** 게시글 목록 응답을 DTO 목록으로 역직렬화한다. */
  private List<UserPostListResponseDto> readUserPosts(MvcResult result) throws Exception {
    return objectMapper.readValue(
        result.getResponse().getContentAsString(),
        new TypeReference<>() {
        }
    );
  }

  private MvcResult performGet(String path, PostType postType) throws Exception {
    return performGet(path, postType, 20);
  }

  private MvcResult performGet(String path, PostType postType, int pageSize) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = get(path).param("size", String.valueOf(pageSize));
    if (postType != null) {
      requestBuilder.param("postType", postType.name());
    }
    return mockMvc.perform(
            mockMvcUtils.addAuthentication(requestBuilder, TestClientDto.fromEntity(client)))
        .andExpect(status().isOk())
        .andReturn();
  }

  private List<Long> expectedUploadedPostIds(PostType postType) {
    return expectedUploadedPostIds(client, postType);
  }

  /** 작성자와 게시글 종류를 기준으로 기대하는 게시글 ID를 정렬해 반환한다. */
  private List<Long> expectedUploadedPostIds(User author, PostType postType) {
    return orderedPostIds(postResult.posts().stream()
        .filter(post -> post.getUser().getUserId().equals(author.getUserId()))
        .toList(), postType);
  }

  private List<Long> expectedBookmarkedPostIds(PostType postType) {
    return orderedPostIds(postResult.posts().stream()
        .filter(post -> bookmarkedPostIds.contains(post.getPostId()))
        .toList(), postType);
  }

  private List<Long> expectedPreferredPostIds(PostType postType) {
    return orderedPostIds(postResult.posts().stream()
        .filter(post -> preferredPostIds.contains(post.getPostId()))
        .toList(), postType);
  }

  private List<Long> orderedPostIds(List<Post> posts, PostType postType) {
    return posts.stream()
        .filter(post -> postType == null || post.getPostType() == postType)
        .sorted(userPostListOrder())
        .map(Post::getPostId)
        .distinct()
        .toList();
  }

  private Comparator<Post> userPostListOrder() {
    return Comparator.comparing(Post::getCreatedAt)
        .reversed()
        .thenComparing(Post::getPostId, Comparator.reverseOrder());
  }

  private Set<Long> createBookmarkAndCustomCategory() {
    Post bookmarkedThemePost = postResult.themeBoards().get(0).getPost();
    Post bookmarkedDesignPost = postResult.designBoards().stream()
        .min(Comparator.comparing(DesignBoard::getDesignBoardId))
        .map(DesignBoard::getPost)
        .orElseThrow();
    Set<Long> bookmarkedIds = Set.of(bookmarkedThemePost.getPostId(),
        bookmarkedDesignPost.getPostId());

    Category bookmark = categoryRepository.save(Category.builder()
        .owner(client)
        .name("bookmark")
        .categoryType(CategoryType.BOOKMARK)
        .build());
    categoryPostRepository.saveAll(List.of(bookmarkedThemePost, bookmarkedDesignPost).stream()
        .map(post -> CategoryPost.builder()
            .category(bookmark)
            .post(post)
            .build())
        .toList());

    customCategoryOnlyPost = postResult.themeBoards().stream()
        .map(ThemeBoard::getPost)
        .filter(post -> post.getUser().getUserId().equals(client.getUserId()))
        .filter(post -> !bookmarkedIds.contains(post.getPostId()))
        .findFirst()
        .orElseThrow();
    Category customCategory = categoryRepository.save(Category.builder()
        .owner(client)
        .name("custom")
        .categoryType(CategoryType.CUSTOM)
        .build());
    categoryPostRepository.save(CategoryPost.builder()
        .category(customCategory)
        .post(customCategoryOnlyPost)
        .build());

    return bookmarkedIds;
  }

  private Set<Long> resolvePreferredPostIds() {
    return postResult.prefers().stream()
        .filter(prefer -> prefer.getUser().getUserId().equals(client.getUserId()))
        .map(prefer -> prefer.getPost().getPostId())
        .collect(Collectors.toSet());
  }
}
