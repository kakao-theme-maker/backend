package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.global.utils.FileManager;
import com.komentum.post.domain.ThemeBoard;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.dto.ThemePreviewDto;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class ThemeRetrieveControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Autowired
  private TestDataRemover testDataRemover;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private ThemeComponentScenarioSupport themeComponentScenarioSupport;

  @Autowired
  private PostScenarioSupport postScenarioSupport;

  @Autowired
  private UserScenarioSupport userScenarioSupport;

  @Autowired
  private DesignComponentScenarioSupport designComponentScenarioSupport;

  @Autowired
  private FileManager fileManager;

  private TestClientDto testClient;

  private void assertThemePreviewDto(ThemePreviewDto themePreviewDto) {
    assertThat(themePreviewDto.getThemeComponentId()).isNotNull();
    assertThat(themePreviewDto.getThemeName()).isNotBlank();
    assertThat(themePreviewDto.getPreviewImageUrl()).isNotBlank();
    assertThat(themePreviewDto.getCreatedAt()).isNotNull();
    assertThat(themePreviewDto.getUpdatedAt()).isNotNull();
  }

  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    themeDataGenerator.generateTestData(10);
    User testUser = userDataGenerator.generateTestUser(themeDataGenerator.userEmail);
    testClient = TestClientDto.fromEntity(testUser);
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("")
  void getAllThemes_success() throws Exception {
    // given
    int pageNumber = 1;
    int pageSize = 3;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/themes")
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        testClient);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(pageSize));
  }

  @Test
  @DisplayName("")
  void getThemeById_success() throws Exception {
    // given
    ThemeComponent toFind = themeDataGenerator.initialThemes.get(0);
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/themes/{id}",
        toFind.getThemeComponentId());
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        testClient);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.themeComponentId").value(toFind.getThemeComponentId()));
  }

  @Test
  @DisplayName("")
  void getPublicThemes_success() throws Exception {
    // given
    int pageNumber = 1;
    int pageSize = 3;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/themes/public")
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        testClient);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  void getThemesByUserEmail_success() throws Exception {
    // given
    String userEmail = themeDataGenerator.userEmail;
    int pageNumber = 1;
    int pageSize = 3;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
            "/api/themes/user/{userEmail}",
            userEmail)
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        testClient);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  void getCompletedThemes_success() throws Exception {
    // given
    int pageNumber = 1;
    int pageSize = 3;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
            "/api/themes/completed")
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        testClient);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  void getCompletedThemesByUser_success() throws Exception {
    // given
    int pageNumber = 1;
    int pageSize = 4;
    String userEmail = themeDataGenerator.userEmail;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
            "/api/themes/completed/user/{userEmail}", userEmail)
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.addAuthentication(requestBuilder,
        testClient);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("when send request, retrieve themes order by prefers")
  public void findPopularThemes_success() throws Exception {
    // 임시 : setUp 데이터 삭제 ( 시나리오 구현 어려움 )
    testDataRemover.deleteAll();
    // stub : 이미지 생성 시 Mock URL 사용
    Mockito.when(fileManager.resolveFilePath(Mockito.any()))
        .thenReturn("http://mocked-url/1234567890");
    // given: 사용자 4명 생성
    List<User> users = userScenarioSupport.builder()
        .withUsers(4)
        .build().users();
    // given : design component 4개 생성
    List<DesignComponent> designComponents = designComponentScenarioSupport.builder(users)
        .withCountPerUser(1)
        .build().designComponents();
    // given: theme 4개 생성
    List<ThemeComponent> themeComponents = themeComponentScenarioSupport.builder(users,
            designComponents)
        .withCountPerUser(1)
        .build().themeComponents();
    // given: theme board를 4개 생성하고, 그 중 2개는 4개의 좋아요를 갖는다
    var postResult = postScenarioSupport.builder(users)
        .withThemeBoards(themeComponents)
        .withPrefersPerPost(4, 0.5)
        .build();
    // when
    User client = users.get(0);
    List<ThemePreviewDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemePreviewDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/themes/popular")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then
    Map<Long, Long> preferCountByPost = postResult.prefers().stream()
        .collect(Collectors.groupingBy(
            p -> p.getPost().getPostId(),
            Collectors.counting()
        ));
    Map<Integer, Long> preferCountByTheme = postResult.themeBoards().stream()
        .collect(Collectors.groupingBy(
            tb -> tb.getThemeComponent().getThemeComponentId(),
            Collectors.summingLong(
                tb -> preferCountByPost.getOrDefault(
                    tb.getPost().getPostId(),
                    0L
                )
            )
        ));
    for (int i = 0; i < 2; i++) {
      assertThemePreviewDto(response.get(i));
      assertThat(preferCountByTheme.get(response.get(i).getThemeComponentId())).isEqualTo(4);
    }
    for (int i = 2; i < 4; i++) {
      assertThemePreviewDto(response.get(i));
      assertThat(preferCountByTheme.get(response.get(i).getThemeComponentId())).isEqualTo(0);
    }
  }

  @Test
  @DisplayName("when send request, retrieve themes that user bookmarked")
  public void findBookmarkedThemes_success() throws Exception {
    // 임시 : setUp 데이터 삭제 ( 시나리오 구현 어려움 )
    testDataRemover.deleteAll();
    // stub : 이미지 생성 시 Mock URL 사용
    Mockito.when(fileManager.resolveFilePath(Mockito.any()))
        .thenReturn("http://mocked-url/1234567890");
    // given: 사용자 4명 생성
    List<User> users = userScenarioSupport.builder()
        .withUsers(4)
        .build().users();
    // given : design component 4개 생성
    List<DesignComponent> designComponents = designComponentScenarioSupport.builder(users)
        .withCountPerUser(1)
        .build().designComponents();
    // given: theme 4개 생성
    List<ThemeComponent> themeComponents = themeComponentScenarioSupport.builder(users,
            designComponents)
        .withCountPerUser(1)
        .build().themeComponents();
    // given: theme board를 4개 생성하고, 그 중 2개를 저장한다
    var postResult = postScenarioSupport.builder(users)
        .withThemeBoards(themeComponents)
        .withBookmarkRatio(0.5)
        .build();
    // when
    User client = users.get(0);
    List<ThemePreviewDto> response = mockMvcUtils.doAuthRequest(
        MockMvcRequestDto.<Void, List<ThemePreviewDto>>builder()
            .mockMvc(mockMvc)
            .path("/api/themes/bookmarked")
            .httpMethod(HttpMethod.GET)
            .clientDto(TestClientDto.fromEntity(client))
            .statusCode(200)
            .responseType(new TypeReference<>() {
            })
            .build()
    );
    // then: 응답 데이터 크기 검증
    Set<Long> bookmarkedPostIdSet = postResult.bookmarkMappings().stream()
        .map(bm -> bm.getPost().getPostId())
        .collect(Collectors.toSet());
    Set<Integer> bookmarkedThemeIdSet = postResult.themeBoards().stream()
        .filter(tb -> bookmarkedPostIdSet.contains(tb.getPost().getPostId()))
        .map(ThemeBoard::getThemeComponent)
        .map(ThemeComponent::getThemeComponentId)
        .collect(Collectors.toSet());
    assertThat(response).hasSize(bookmarkedThemeIdSet.size());
    // then: 응답 데이터의 각 테마가 실제로 북마크 되어있는지 검증
    assertThat(response).allSatisfy(dto -> {
      assertThat(bookmarkedThemeIdSet).contains(dto.getThemeComponentId());
      assertThemePreviewDto(dto);
    });
  }
}