package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.dto.CreateComponentTypeRequest;
import com.komentum.theme.component.dto.UpdateComponentTypeRequest;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ComponentTypeRepository;
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
@DisplayName("ComponentType 테스트")
class ComponentTypeControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ComponentTypeRepository componentTypeRepository;

  @BeforeEach
  void setUp() {
    componentTypeRepository.deleteAll();
  }

  @AfterEach
  void tearDown() {
    componentTypeRepository.deleteAll();
  }

  @Nested
  @DisplayName("ComponentType CRUD 테스트")
  class ComponentTypeTests {

    @Test
    @DisplayName("ComponentType 생성 테스트")
    void createComponentType() throws Exception {
      // Given
      CreateComponentTypeRequest request = CreateComponentTypeRequest.builder()
          .explain("버튼 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/components/button")
          .componentName("Button")
          .sizeX(100)
          .sizeY(50)
          .build();

      // When & Then
      mockMvc.perform(post("/api/component-types")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("버튼 컴포넌트"))
          .andExpect(jsonPath("$.componentName").value("Button"))
          .andExpect(jsonPath("$.sizeX").value(100))
          .andExpect(jsonPath("$.sizeY").value(50));

      assertThat(componentTypeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("ComponentType 조회 테스트")
    void getComponentType() throws Exception {
      // Given
      ComponentType savedComponent = componentTypeRepository.save(ComponentType.builder()
          .explain("테스트 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/test")
          .componentName("TestComponent")
          .sizeX(200)
          .sizeY(100)
          .build());

      // When & Then
      mockMvc.perform(get("/api/component-types/{id}", savedComponent.getComponentTypeId()))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.componentTypeId").value(savedComponent.getComponentTypeId()))
          .andExpect(jsonPath("$.componentName").value("TestComponent"));
    }

    @Test
    @DisplayName("ComponentType 전체 조회 테스트")
    void getAllComponentTypes() throws Exception {
      // Given
      componentTypeRepository.save(ComponentType.builder()
          .explain("컴포넌트1")
          .platform(Platform.ANDROID)
          .componentPath("/comp1")
          .componentName("Component1")
          .build());

      componentTypeRepository.save(ComponentType.builder()
          .explain("컴포넌트2")
          .platform(Platform.IOS)
          .componentPath("/comp2")
          .componentName("Component2")
          .build());

      // When & Then
      mockMvc.perform(get("/api/component-types"))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("ComponentType 수정 테스트")
    void updateComponentType() throws Exception {
      // Given
      ComponentType savedComponent = componentTypeRepository.save(ComponentType.builder()
          .explain("원본 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/original")
          .componentName("OriginalComponent")
          .sizeX(100)
          .sizeY(50)
          .build());

      UpdateComponentTypeRequest updateRequest = UpdateComponentTypeRequest.builder()
          .explain("수정된 컴포넌트")
          .platform(Platform.IOS)
          .componentPath("/updated")
          .componentName("UpdatedComponent")
          .sizeX(150)
          .sizeY(75)
          .build();

      // When & Then
      mockMvc.perform(put("/api/component-types/{id}", savedComponent.getComponentTypeId())
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(updateRequest)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.explain").value("수정된 컴포넌트"))
          .andExpect(jsonPath("$.componentName").value("UpdatedComponent"));
    }

    @Test
    @DisplayName("ComponentType 삭제 테스트")
    void deleteComponentType() throws Exception {
      // Given
      ComponentType savedComponent = componentTypeRepository.save(ComponentType.builder()
          .explain("삭제될 컴포넌트")
          .platform(Platform.ANDROID)
          .componentPath("/delete")
          .componentName("DeleteComponent")
          .build());

      // When & Then
      mockMvc.perform(delete("/api/component-types/{id}", savedComponent.getComponentTypeId()))
          .andExpect(status().isOk());

      assertThat(componentTypeRepository.existsById(savedComponent.getComponentTypeId())).isFalse();
    }
  }
}