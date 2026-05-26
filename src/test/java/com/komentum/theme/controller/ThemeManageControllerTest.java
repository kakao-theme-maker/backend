package com.komentum.theme.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.test.MockMvcUtils;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.test.data.UserDataGenerator;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.dto.CreateThemeRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@SpringBootTest
@EnableTestProfile
@AutoConfigureMockMvc
class ThemeManageControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  @Autowired
  private UserDataGenerator userDataGenerator;

  @Autowired
  private MockMvcUtils mockMvcUtils;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    userDataGenerator.deleteAllUsers();
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
  public void createTheme_success() throws Exception {
    // given
    CreateThemeRequest createThemeRequest = CreateThemeRequest.builder()
        .themeName("themeName")
        .images(themeDataGenerator.getImageRequests())
        .styles(themeDataGenerator.getStyleRequests())
        .isPublic(true)
        .versionName("versionName")
        .userEmail("test@test.com")
        .build();
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.post("/api/themes")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createThemeRequest));
    request = mockMvcUtils.addAuthentication(request, themeDataGenerator.userEmail);
    // then
    mockMvc.perform(request)
        .andExpect(status().isCreated());
  }

  @Test
  @DisplayName("")
  public void updateTheme_success() throws Exception {
    // given
    CreateThemeRequest createThemeRequest = CreateThemeRequest.builder()
        .themeName("updated")
        .images(themeDataGenerator.getImageRequests())
        .styles(themeDataGenerator.getStyleRequests())
        .isPublic(true)
        .versionName("updated")
        .userEmail("test@test.com")
        .build();
    ThemeComponent target = themeDataGenerator.initialThemes.get(0);
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/themes/{id}",
            target.getThemeComponentId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(createThemeRequest));
    request = mockMvcUtils.addAuthentication(request, themeDataGenerator.userEmail);
    // then
    mockMvc.perform(request)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  public void markThemeAsDone_success() throws Exception {
    // given
    ThemeComponent target = themeDataGenerator.initialThemes.get(0);
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.put("/api/themes/{id}/done",
        target.getThemeComponentId());
    request = mockMvcUtils.addAuthentication(request, themeDataGenerator.userEmail);
    // then
    mockMvc.perform(request)
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("")
  public void deleteTheme_success() throws Exception {
    // given
    ThemeComponent target = themeDataGenerator.initialThemes.get(0);
    // when
    MockHttpServletRequestBuilder request = MockMvcRequestBuilders.delete("/api/themes/{id}",
        target.getThemeComponentId());
    request = mockMvcUtils.addAuthentication(request, themeDataGenerator.userEmail);
    // then
    mockMvc.perform(request)
        .andExpect(status().isNoContent());
  }
}