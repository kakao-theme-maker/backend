package com.komentum.post.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.global.utils.DateUtils;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.Post;
import com.komentum.post.domain.enums.PostType;
import com.komentum.post.dto.PostDto.UserPostListResponseDto;
import com.komentum.post.repository.PostRepository;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.TestClientDto;
import com.komentum.test.dto.TestParams;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.user.domain.User;
import java.time.LocalDateTime;
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
  private PostRepository postRepository;

  private User client;
  private User otherUser;
  private PostScenarioSupport.Result postResult;
  private Map<Long, Post> postById;
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
    otherUser = users.get(1);

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
    MvcResult result = performGet("/api/users/me/upload-posts", null, client, null, null);
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
    assertThat(first.has("bookmarked")).isFalse();
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
  @DisplayName("좋아요한 게시글 목록은 좋아요 필드를 유지하고 북마크 필드를 제외한다")
  void preferredPostListResponse_keepsPreferFieldsAndExcludesBookmark() throws Exception {
    MvcResult result = performGet("/api/users/me/preferred-posts", null, client, null, null);
    JsonNode first = objectMapper.readTree(result.getResponse().getContentAsString()).get(0);

    assertThat(first.get("prefers").asLong()).isEqualTo(1L);
    assertThat(first.get("preferred").asBoolean()).isTrue();
    assertThat(first.has("bookmarked")).isFalse();
  }

  @Test
  @DisplayName("좋아요 등록과 취소는 좋아요한 게시글 목록과 좋아요 수에 반영된다")
  void preferLifecycle_isReflectedInPreferredPostList() throws Exception {
    Post targetPost = postResult.themeBoards().get(0).getPost();

    performSavePrefer(targetPost, otherUser);
    assertThat(requestPreferCount(targetPost)).isEqualTo(2L);
    UserPostListResponseDto preferredPost = requestUserPosts(
        "/api/users/me/preferred-posts", null, client).stream()
        .filter(dto -> dto.getPostId().equals(targetPost.getPostId()))
        .findFirst()
        .orElseThrow();
    assertThat(preferredPost.getPrefers()).isEqualTo(2L);
    assertThat(preferredPost.isPreferred()).isTrue();

    performDeletePrefer(targetPost, client);
    assertThat(requestPreferCount(targetPost)).isEqualTo(1L);
    assertThat(requestUserPosts("/api/users/me/preferred-posts", null, client))
        .extracting(UserPostListResponseDto::getPostId)
        .doesNotContain(targetPost.getPostId());

    performDeletePrefer(targetPost, otherUser);
    assertThat(requestPreferCount(targetPost)).isZero();
    assertThat(requestUserPosts("/api/users/me/preferred-posts", null, otherUser)).isEmpty();
  }

  @Test
  @DisplayName("좋아요한 게시글 목록은 인증된 사용자의 좋아요만 반환한다")
  void preferredPostList_isIsolatedByUser() throws Exception {
    Post targetPost = postResult.themeBoards().get(0).getPost();
    performDeletePrefer(targetPost, client);
    performSavePrefer(targetPost, otherUser);

    assertThat(requestUserPosts("/api/users/me/preferred-posts", null, client))
        .extracting(UserPostListResponseDto::getPostId)
        .doesNotContain(targetPost.getPostId());
    assertThat(requestUserPosts("/api/users/me/preferred-posts", null, otherUser))
        .singleElement()
        .satisfies(dto -> {
          assertThat(dto.getPostId()).isEqualTo(targetPost.getPostId());
          assertThat(dto.getPrefers()).isEqualTo(1L);
          assertThat(dto.isPreferred()).isTrue();
        });
  }

  @Test
  @DisplayName("좋아요한 게시글 목록은 생성일시와 게시글 ID 내림차순으로 결정적 페이징한다")
  void preferredPostList_paginatesInDeterministicOrder() throws Exception {
    LocalDateTime baseTime = LocalDateTime.of(2026, 1, 1, 0, 0);
    List<Post> posts = postResult.posts();
    for (int i = 0; i < posts.size(); i++) {
      posts.get(i).setCreatedAt(baseTime.plusDays(i / 2));
    }
    postRepository.saveAllAndFlush(posts);

    assertPreferredPostPages(null, 3);
    assertPreferredPostPages(PostType.THEME_BOARD, 1);
  }

  private void assertPreferredPostPages(PostType postType, int pageSize) throws Exception {
    List<Long> expectedPostIds = expectedPreferredPostIds(postType);
    int lastPage = (expectedPostIds.size() - 1) / pageSize;
    for (int page = 0; page <= lastPage + 1; page++) {
      int fromIndex = Math.min(page * pageSize, expectedPostIds.size());
      int toIndex = Math.min(fromIndex + pageSize, expectedPostIds.size());

      assertThat(requestUserPosts(
          "/api/users/me/preferred-posts", postType, client, page, pageSize))
          .extracting(UserPostListResponseDto::getPostId)
          .containsExactlyElementsOf(expectedPostIds.subList(fromIndex, toIndex));
    }
  }

  @Test
  @DisplayName("여러 디자인 에셋이 연결된 게시글도 좋아요 목록에서 한 행만 반환한다")
  void preferredDesignPostList_returnsOneRowPerPost() throws Exception {
    Map<Long, Long> designBoardCountByPostId = postResult.designBoards().stream()
        .collect(Collectors.groupingBy(
            designBoard -> designBoard.getPost().getPostId(),
            Collectors.counting()));
    assertThat(designBoardCountByPostId.values()).allMatch(count -> count > 1);

    List<UserPostListResponseDto> response = requestUserPosts(
        "/api/users/me/preferred-posts", PostType.DESIGN_BOARD);
    assertThat(response)
        .extracting(UserPostListResponseDto::getPostId)
        .doesNotHaveDuplicates()
        .containsExactlyElementsOf(expectedPreferredPostIds(PostType.DESIGN_BOARD));
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
    });
  }

  private List<UserPostListResponseDto> requestUserPosts(String path, PostType postType)
      throws Exception {
    return requestUserPosts(path, postType, client);
  }

  private List<UserPostListResponseDto> requestUserPosts(String path, PostType postType, User user)
      throws Exception {
    return requestUserPosts(path, postType, user, null, null);
  }

  private List<UserPostListResponseDto> requestUserPosts(String path, PostType postType, User user,
      Integer page, Integer size) throws Exception {
    MvcResult result = performGet(path, postType, user, page, size);
    return objectMapper.readValue(
        result.getResponse().getContentAsString(),
        new TypeReference<>() {
        }
    );
  }

  private MvcResult performGet(String path, PostType postType, User user, Integer page, Integer size)
      throws Exception {
    MockHttpServletRequestBuilder requestBuilder = get(path);
    if (postType != null) {
      requestBuilder.param("postType", postType.name());
    }
    if (page != null && size != null) {
      requestBuilder.params(TestParams.withPaging(page, size));
    }
    return mockMvc.perform(
            mockMvcUtils.addAuthentication(requestBuilder, TestClientDto.fromEntity(user)))
        .andExpect(status().isOk())
        .andReturn();
  }

  private void performSavePrefer(Post postToPrefer, User user) throws Exception {
    mockMvc.perform(mockMvcUtils.addAuthentication(
            post("/api/posts/{postId}/prefer", postToPrefer.getPostId()),
            TestClientDto.fromEntity(user)))
        .andExpect(status().isOk());
  }

  private void performDeletePrefer(Post postToUnprefer, User user) throws Exception {
    mockMvc.perform(mockMvcUtils.addAuthentication(
            delete("/api/posts/{postId}/prefer", postToUnprefer.getPostId()),
            TestClientDto.fromEntity(user)))
        .andExpect(status().isNoContent());
  }

  private long requestPreferCount(Post post) throws Exception {
    MvcResult result = mockMvc.perform(get("/api/posts/{postId}/prefer", post.getPostId()))
        .andExpect(status().isOk())
        .andReturn();
    return Long.parseLong(result.getResponse().getContentAsString());
  }

  private List<Long> expectedUploadedPostIds(PostType postType) {
    return orderedPostIds(postResult.posts().stream()
        .filter(post -> post.getUser().getUserId().equals(client.getUserId()))
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

  private Set<Long> resolvePreferredPostIds() {
    return postResult.prefers().stream()
        .filter(prefer -> prefer.getUser().getUserId().equals(client.getUserId()))
        .map(prefer -> prefer.getPost().getPostId())
        .collect(Collectors.toSet());
  }
}
