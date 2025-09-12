package com.komentum.theme.theme.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.komentum.config.EnableTestProfile;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.ThemeDataGenerator;
import com.komentum.test.UserDataGenerator;
import com.komentum.theme.theme.domain.ThemeComponent;
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


  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    themeDataGenerator.generateTestData(10);
    userDataGenerator.generateTestUser(themeDataGenerator.userEmail);
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
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.generateAuthJsonRequest(requestBuilder,
        themeDataGenerator.userEmail);
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
    requestBuilder = mockMvcUtils.generateAuthJsonRequest(requestBuilder,
        themeDataGenerator.userEmail);
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
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.generateAuthJsonRequest(requestBuilder,
        themeDataGenerator.userEmail);
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
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.generateAuthJsonRequest(requestBuilder,
        themeDataGenerator.userEmail);
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
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.generateAuthJsonRequest(requestBuilder,
        themeDataGenerator.userEmail);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  void getCompletedThemesByUser_success() throws Exception {
    // given
    int pageNumber = 1;
    int pageSize = 3;
    String userEmail = themeDataGenerator.userEmail;
    // when
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(
            "/api/themes/completed/user/{userEmail}", userEmail)
        .param("pageNumber", String.valueOf(pageNumber))
        .param("pageSize", String.valueOf(pageSize));
    requestBuilder = mockMvcUtils.generateAuthJsonRequest(requestBuilder,
        themeDataGenerator.userEmail);
    // then
    mockMvc.perform(requestBuilder)
        .andExpect(status().isOk());

  }
}