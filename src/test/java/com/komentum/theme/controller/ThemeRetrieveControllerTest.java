package com.komentum.theme.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.test.dto.TestClientDto;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.service.ThemeImageService;
import com.komentum.user.domain.User;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private ThemeImageService themeImageService;

  private TestClientDto testClient;


  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    themeDataGenerator.generateTestData(10);
    User testUser = userDataGenerator.generateTestUser(themeDataGenerator.userEmail);
    testClient = TestClientDto.fromEntity(testUser);
  }

  @AfterEach
  void tearDown() {
    themeDataGenerator.deleteTestData();
    userDataGenerator.deleteAllUsers();
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
        .andExpect(jsonPath("$.length()").value(pageSize))
        .andExpect(jsonPath("$[0].createdAt").exists())
        .andExpect(jsonPath("$[0].previewImageUrl").isNotEmpty());
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
        .andExpect(jsonPath("$.themeComponentId").value(toFind.getThemeComponentId()))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.previewImageUrl").value(
            findPreviewImageUrl(toFind.getThemeComponentId())));
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

  private String findPreviewImageUrl(Integer themeComponentId) {
    return themeImageService.findThemePreviewImages(List.of(themeComponentId))
        .get(themeComponentId);
  }
}
