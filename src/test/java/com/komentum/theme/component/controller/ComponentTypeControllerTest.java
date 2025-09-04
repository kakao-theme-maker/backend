package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.service.ComponentTypeService;
import com.komentum.theme.exception.ResourceNotFoundException;
import com.komentum.theme.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ComponentTypeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ComponentTypeService componentTypeService;

    @InjectMocks
    private ComponentTypeController componentTypeController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(componentTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createComponentType_ShouldReturnCreatedComponentType() throws Exception {
        ComponentType componentType = ComponentType.builder()
                .explain("버튼 컴포넌트")
                .iosComponentPath("/ios/button")
                .androidComponentPath("/android/button")
                .sizeX(100)
                .sizeY(50)
                .build();

        ComponentType savedType = ComponentType.builder()
                .componentTypeId(1)
                .explain("버튼 컴포넌트")
                .iosComponentPath("/ios/button")
                .androidComponentPath("/android/button")
                .sizeX(100)
                .sizeY(50)
                .build();

        when(componentTypeService.createComponentType(any(ComponentType.class))).thenReturn(savedType);

        mockMvc.perform(post("/api/component-types")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(componentType)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentTypeId").value(1))
                .andExpect(jsonPath("$.explain").value("버튼 컴포넌트"))
                .andExpect(jsonPath("$.sizeX").value(100));

        verify(componentTypeService).createComponentType(any(ComponentType.class));
    }

    @Test
    void getComponentTypeById_WithValidId_ShouldReturnComponentType() throws Exception {
        ComponentType componentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("조회 테스트")
                .iosComponentPath("/ios/test")
                .build();

        when(componentTypeService.getComponentTypeById(1)).thenReturn(componentType);

        mockMvc.perform(get("/api/component-types/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.componentTypeId").value(1))
                .andExpect(jsonPath("$.explain").value("조회 테스트"));

        verify(componentTypeService).getComponentTypeById(1);
    }

    @Test
    void getComponentTypeById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(componentTypeService.getComponentTypeById(999))
                .thenThrow(new ResourceNotFoundException("ComponentType not found with id: 999"));

        mockMvc.perform(get("/api/component-types/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ComponentType not found with id: 999"));

        verify(componentTypeService).getComponentTypeById(999);
    }

    @Test
    void getAllComponentTypes_ShouldReturnComponentTypeList() throws Exception {
        List<ComponentType> componentTypes = Arrays.asList(
                ComponentType.builder()
                        .componentTypeId(1)
                        .explain("버튼")
                        .build(),
                ComponentType.builder()
                        .componentTypeId(2)
                        .explain("이미지")
                        .build()
        );

        when(componentTypeService.getAllComponentTypes()).thenReturn(componentTypes);

        mockMvc.perform(get("/api/component-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].explain").value("버튼"))
                .andExpect(jsonPath("$[1].explain").value("이미지"));

        verify(componentTypeService).getAllComponentTypes();
    }

    @Test
    void updateComponentType_WithValidData_ShouldReturnUpdatedComponentType() throws Exception {
        ComponentType updateRequest = ComponentType.builder()
                .explain("수정된 설명")
                .sizeX(120)
                .build();

        ComponentType updatedType = ComponentType.builder()
                .componentTypeId(1)
                .explain("수정된 설명")
                .iosComponentPath("/ios/button")
                .sizeX(120)
                .sizeY(50)
                .build();

        when(componentTypeService.updateComponentType(eq(1), any(ComponentType.class))).thenReturn(updatedType);

        mockMvc.perform(put("/api/component-types/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explain").value("수정된 설명"))
                .andExpect(jsonPath("$.sizeX").value(120));

        verify(componentTypeService).updateComponentType(eq(1), any(ComponentType.class));
    }

    @Test
    void updateComponentType_WithInvalidId_ShouldReturnNotFound() throws Exception {
        ComponentType updateRequest = ComponentType.builder()
                .explain("수정된 설명")
                .build();

        when(componentTypeService.updateComponentType(eq(999), any(ComponentType.class)))
                .thenThrow(new ResourceNotFoundException("ComponentType not found with id: 999"));

        mockMvc.perform(put("/api/component-types/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ComponentType not found with id: 999"));

        verify(componentTypeService).updateComponentType(eq(999), any(ComponentType.class));
    }

    @Test
    void deleteComponentType_WithValidId_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/component-types/1"))
                .andExpect(status().isOk());

        verify(componentTypeService).deleteComponentType(1);
    }

    @Test
    void deleteComponentType_WithInvalidId_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("ComponentType not found with id: 999"))
                .when(componentTypeService).deleteComponentType(999);

        mockMvc.perform(delete("/api/component-types/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ComponentType not found with id: 999"));

        verify(componentTypeService).deleteComponentType(999);
    }
}