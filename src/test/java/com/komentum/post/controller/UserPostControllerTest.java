package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.domain.DesignComponent;
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

  private User client;
  private Post customCategoryOnlyPost;
  private PostScenarioSupport.Result postResult;
  private Map<Long, Post> postById;
  private Set<Long> bookmarkedPostIds;
  private Set<Long> preferredPostIds;

  @BeforeEach
  void setUp() {
    given(fileManager.resolveFilePath(any())).willReturn(PREVIEW_IMAGE_URL);
    given(fileManager.convertUrlToFileName(any())).willReturn(PREVIEW_IMAGE_NAME);

    List<User> users = userScenarioSupport.builder()
        .withUsers(3)
        .build()
        .users();
    client = users.get(0);

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
    MvcResult result = performGet(path, postType);
    return objectMapper.readValue(
        result.getResponse().getContentAsString(),
        new TypeReference<>() {
        }
    );
  }

  private MvcResult performGet(String path, PostType postType) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = get(path);
    if (postType != null) {
      requestBuilder.param("postType", postType.name());
    }
    return mockMvc.perform(
            mockMvcUtils.addAuthentication(requestBuilder, TestClientDto.fromEntity(client)))
        .andExpect(status().isOk())
        .andReturn();
  }

  private List<Long> expectedUploadedPostIds(PostType postType) {
    return orderedPostIds(postResult.posts().stream()
        .filter(post -> post.getUser().getUserId().equals(client.getUserId()))
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
