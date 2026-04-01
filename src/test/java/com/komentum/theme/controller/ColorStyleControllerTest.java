package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.dto.CreateColorStyleRequest;
import com.komentum.theme.component.dto.UpdateColorStyleRequest;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("ColorStyle 테스트")
class ColorStyleControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ColorStyleRepository colorStyleRepository;

  @BeforeEach
  void setUp() {
    colorStyleRepository.deleteAll();
  }

  @AfterEach
  void tearDown() {
    colorStyleRepository.deleteAll();
  }

  @Nested
  @DisplayName("ColorStyle CRUD 테스트")
  class ColorStyleTests {

    @Test
    @DisplayName("ColorStyle 생성 테스트")
    void createColorStyle() throws Exception {
      // Given
      CreateColorStyleRequest request = CreateColorStyleRequest.builder()
          .explain("기본 색상 스타일")
          .platform(Platform.ANDROID)
          .styleSheetPath("/styles/colors.css")
          .styleElementName("primaryColor")
          .stylePropsName("color")
          .build();

      // When & Then
      mockMvc.perform(post("/api/color-styles")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("기본 색상 스타일"))
          .andExpect(jsonPath("$.platform").value("ANDROID"))
          .andExpect(jsonPath("$.styleSheetPath").value("/styles/colors.css"))
          .andExpect(jsonPath("$.styleElementName").value("primaryColor"))
          .andExpect(jsonPath("$.stylePropsName").value("color"));

      // 데이터베이스 검증
      assertThat(colorStyleRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("ColorStyle 조회 테스트")
    void getColorStyle() throws Exception {
      // Given
      ColorStyle savedColorStyle = colorStyleRepository.save(ColorStyle.builder()
          .explain("테스트 색상")
          .platform(Platform.ANDROID)
          .styleSheetPath("/test.css")
          .styleElementName("testColor")
          .stylePropsName("background-color")
          .build());

      // When & Then
      mockMvc.perform(get("/api/color-styles/{id}", savedColorStyle.getColorStyleId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.colorStyleId").value(savedColorStyle.getColorStyleId()))
          .andExpect(jsonPath("$.explain").value("테스트 색상"));
    }

    @Test
    @DisplayName("ColorStyle 전체 조회 테스트")
    void getAllColorStyles() throws Exception {
      // Given
      colorStyleRepository.save(ColorStyle.builder()
          .explain("색상1")
          .platform(Platform.ANDROID)
          .styleSheetPath("/test1.css")
          .styleElementName("color1")
          .stylePropsName("color")
          .build());

      colorStyleRepository.save(ColorStyle.builder()
          .explain("색상2")
          .platform(Platform.IOS)
          .styleSheetPath("/test2.css")
          .styleElementName("color2")
          .stylePropsName("background")
          .build());

      // When & Then
      mockMvc.perform(get("/api/color-styles"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("ColorStyle 수정 테스트")
    void updateColorStyle() throws Exception {
      // Given
      ColorStyle savedColorStyle = colorStyleRepository.save(ColorStyle.builder()
          .explain("원본 색상")
          .platform(Platform.ANDROID)
          .styleSheetPath("/original.css")
          .styleElementName("originalColor")
          .stylePropsName("color")
          .build());

      UpdateColorStyleRequest updateRequest = UpdateColorStyleRequest.builder()
          .explain("수정된 색상")
          .platform(Platform.IOS)
          .styleSheetPath("/updated.css")
          .styleElementName("updatedColor")
          .stylePropsName("background-color")
          .build();

      // When & Then
      mockMvc.perform(put("/api/color-styles/{id}", savedColorStyle.getColorStyleId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("수정된 색상"))
          .andExpect(jsonPath("$.platform").value("IOS"));
    }

    @Test
    @DisplayName("ColorStyle 삭제 테스트")
    void deleteColorStyle() throws Exception {
      // Given
      ColorStyle savedColorStyle = colorStyleRepository.save(ColorStyle.builder()
          .explain("삭제될 색상")
          .platform(Platform.ANDROID)
          .styleSheetPath("/delete.css")
          .styleElementName("deleteColor")
          .stylePropsName("color")
          .build());

      // When & Then
      mockMvc.perform(delete("/api/color-styles/{id}", savedColorStyle.getColorStyleId()))
          .andExpect(status().isNoContent());

      // 데이터베이스 검증
      assertThat(colorStyleRepository.existsById(savedColorStyle.getColorStyleId())).isFalse();
    }
  }

}