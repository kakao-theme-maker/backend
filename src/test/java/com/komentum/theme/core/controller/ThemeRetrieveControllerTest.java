package com.komentum.theme.core.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.enums.TypeCodeGroup;
import com.komentum.global.utils.FileManager;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.TestDataRemover;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport;
import com.komentum.test.data.scenario.DesignComponentScenarioSupport.DesignComponentScenarioResult;
import com.komentum.test.data.scenario.PostScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport;
import com.komentum.test.data.scenario.ThemeComponentScenarioSupport.ThemeComponentScenarioResult;
import com.komentum.test.data.scenario.UserScenarioSupport;
import com.komentum.test.data.scenario.UserScenarioSupport.UserScenarioResult;
import com.komentum.test.dto.MockMvcRequestDto;
import com.komentum.test.dto.MockMvcRequestDto.ExecutionContext;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.dto.ThemeComponentDto;
import com.komentum.theme.core.dto.ThemeDetailResponse;
import com.komentum.theme.core.dto.ThemeDetailResponse.TypeCodeInfo;
import com.komentum.theme.core.dto.ThemePreviewDto;
import com.komentum.user.domain.User;
import java.util.List;
import java.util.Map;
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
import org.springframework.test.web.servlet.ResultActions;
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
  private User testUser;

  UserScenarioResult userResult;
  DesignComponentScenarioResult dcResult;
  ThemeComponentScenarioResult tcResult;
  int tcPerUser = 3;

  private void assertThemePreviewDto(ThemePreviewDto themePreviewDto) {
    assertThat(themePreviewDto.getThemeComponentId()).isNotNull();
    assertThat(themePreviewDto.getThemeName()).isNotBlank();
    assertThat(themePreviewDto.getPreviewImageUrl()).isNotBlank();
    assertThat(themePreviewDto.getCreatedAt()).isNotNull();
    assertThat(themePreviewDto.getUpdatedAt()).isNotNull();
  }

  @BeforeEach
  void setUp() {
    // stub
    Mockito.when(fileManager.resolveFilePath(Mockito.any()))
        .thenReturn("http://mocked-url/1234567890");
    Mockito.when(fileManager.convertUrlToFileName(Mockito.any()))
        .thenReturn("mocked-file-name");
    Mockito.when(fileManager.uploadFile(Mockito.any(), Mockito.any()))
        .thenReturn("http://mocked-url/1234567890");
    // generate users
    userResult = userScenarioSupport.builder()
        .withUsers(3)
        .build();
    // generate design components
    dcResult = designComponentScenarioSupport.builder(userResult.users())
        .withCountPerUser(5)
        .build();
    // generate theme components
    tcResult = themeComponentScenarioSupport.builder(userResult.users(),
            dcResult.designComponents())
        .withCountPerUser(tcPerUser)
        .build();
    System.out.println();
  }

  @AfterEach
  void tearDown() {
    testDataRemover.deleteAll();
  }

  @Test
  @DisplayName("인증되지 않은 사용자가 모든 테마를 조회한다")
  void getAllThemes_success() throws Exception {
    // given
    int pageNumber = 1;
    int pageSize = 3;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/themes")
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(pageSize));
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(pageSize))
        .andExpect(jsonPath("$[0].createdAt").exists())
        .andExpect(jsonPath("$[0].previewImageUrl").isNotEmpty());
  }

  @Test
  @DisplayName("사용자가 특정 테마를 상세 조회한다")
  void findThemeById_success() throws Exception {
    // given
    ThemeComponent toFind = tcResult.themeComponents().get(0);
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/api/themes/{id}",
        toFind.getThemeComponentId());
    ResultActions resultActions = mockMvcUtils.performRequest(requestBuilder,
        ExecutionContext.builder()
            .mockMvc(mockMvc)
            .build());
    // then
    resultActions.andExpect(status().isOk());
    ThemeDetailResponse response = mockMvcUtils.parseResponse(resultActions, new TypeReference<>() {
    });
    assertThat(response.getThemeComponentId()).isEqualTo(toFind.getThemeComponentId());
    assertThat(response.getThemeName()).isEqualTo(toFind.getThemeName());
    assertThat(response.getTypeCodes()).isNotEmpty();
    assertThat(response.getStyleCodes()).isNotEmpty();
    for (TypeCode key : response.getTypeCodes().keySet()) {
      TypeCodeInfo typeCodeInfo = response.getTypeCodes().get(key);
      assertThat(typeCodeInfo.getDesignComponentId()).isNotNull();
      assertThat(typeCodeInfo.getImageUrl()).isNotBlank();
      assertThat(typeCodeInfo.getTypeCodeGroup()).isNotNull();
      assertThat(typeCodeInfo.getTypeCodeGroupName()).isNotBlank();
      if (key.getTypeCodeGroup().equals(TypeCodeGroup.CHATROOM_BUBBLE)) {
        assertThat(typeCodeInfo.getInset()).isNotNull();
      }
    }
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
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("인증되지 않은 사용자가 public user Id로 테마를 조회한다")
  void getThemeByPublicUserId_successWithoutAuthentication() throws Exception {
    // given
    User client = userResult.getFirstUser();
    String publicUserId = client.getPublicUserId();
    int pageNumber = 0;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
            "/api/themes/user/{publicUserId}",
            publicUserId)
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(tcPerUser));
    // then
    ResultActions result = mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
    List<ThemeComponentDto> response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    assertThat(response).hasSize(tcPerUser);
  }

  @Test
  @DisplayName("인증되지 않은 사용자가 완성된 테마 목록을 조회한다")
  void getCompletedThemes_success() throws Exception {
    // given
    int pageNumber = 1;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
            "/api/themes/completed")
        .param("page", String.valueOf(pageNumber))
        .param("size", String.valueOf(tcPerUser));
    // then
    ResultActions result = mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
    List<ThemeComponentDto> response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
    assertThat(response).hasSize(tcPerUser);
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
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("when send request, retrieve themes order by prefers")
  public void findPopularThemes_success() throws Exception {
    // given: theme board를 9개 생성하고, 그 중 4개는 2개의 좋아요를 갖는다
    List<ThemeComponent> themeComponents = tcResult.themeComponents(); // 테마 9개
    var postResult = postScenarioSupport.builder(userResult.users())
        .withThemeBoards(themeComponents)
        .withPrefersPerPost(2, 0.5)
        .build();
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
        "/api/themes/popular");
    ResultActions result = mockMvcUtils.performRequest(requestBuilder, ExecutionContext.builder()
        .mockMvc(mockMvc)
        .build()).andExpect(status().isOk());
    List<ThemePreviewDto> response = mockMvcUtils.parseResponse(result, new TypeReference<>() {
    });
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
    for (int i = 0; i < 4; i++) {
      assertThemePreviewDto(response.get(i));
      assertThat(preferCountByTheme.get(response.get(i).getThemeComponentId())).isEqualTo(2);
    }
    for (int i = 4; i < 9; i++) {
      assertThemePreviewDto(response.get(i));
      assertThat(preferCountByTheme.get(response.get(i).getThemeComponentId())).isEqualTo(0);
    }
  }

}
