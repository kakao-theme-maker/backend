package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.service.DesignComponentService;
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

import java.time.LocalDateTime;
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
class DesignComponentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DesignComponentService designComponentService;

    @InjectMocks
    private DesignComponentController designComponentController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(designComponentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createDesignComponent_ShouldReturnCreatedDesignComponent() throws Exception {
        CreateDesignComponentRequest request = new CreateDesignComponentRequest();
        request.setUserEmail("user@example.com");
        request.setImageUrl("https://example.com/image.png");
        request.setComponentTypeId(1);
        request.setIsPublic(true);

        ComponentType componentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("버튼")
                .build();

        DesignComponent savedComponent = DesignComponent.builder()
                .designComponentId(1)
                .userEmail("user@example.com")
                .imageUrl("https://example.com/image.png")
                .componentType(componentType)
                .isPublic(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(designComponentService.createDesignComponent(any(CreateDesignComponentRequest.class)))
                .thenReturn(savedComponent);

        mockMvc.perform(post("/api/design-components")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designComponentId").value(1))
                .andExpect(jsonPath("$.userEmail").value("user@example.com"))
                .andExpect(jsonPath("$.componentType.componentTypeId").value(1));

        verify(designComponentService).createDesignComponent(any(CreateDesignComponentRequest.class));
    }

    @Test
    void getDesignComponentById_WithValidId_ShouldReturnDesignComponent() throws Exception {
        ComponentType componentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("버튼")
                .build();

        DesignComponent designComponent = DesignComponent.builder()
                .designComponentId(1)
                .userEmail("user@example.com")
                .imageUrl("https://example.com/image.png")
                .componentType(componentType)
                .isPublic(true)
                .build();

        when(designComponentService.getDesignComponentById(1)).thenReturn(designComponent);

        mockMvc.perform(get("/api/design-components/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designComponentId").value(1))
                .andExpect(jsonPath("$.userEmail").value("user@example.com"));

        verify(designComponentService).getDesignComponentById(1);
    }

    @Test
    void getDesignComponentById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(designComponentService.getDesignComponentById(999))
                .thenThrow(new ResourceNotFoundException("DesignComponent not found with id: 999"));

        mockMvc.perform(get("/api/design-components/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("DesignComponent not found with id: 999"));

        verify(designComponentService).getDesignComponentById(999);
    }

    @Test
    void getAllDesignComponents_ShouldReturnDesignComponentList() throws Exception {
        List<DesignComponent> designComponents = Arrays.asList(
                DesignComponent.builder()
                        .designComponentId(1)
                        .userEmail("user1@example.com")
                        .imageUrl("https://example.com/image1.png")
                        .build(),
                DesignComponent.builder()
                        .designComponentId(2)
                        .userEmail("user2@example.com")
                        .imageUrl("https://example.com/image2.png")
                        .build()
        );

        when(designComponentService.getAllDesignComponents()).thenReturn(designComponents);

        mockMvc.perform(get("/api/design-components"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].userEmail").value("user1@example.com"))
                .andExpect(jsonPath("$[1].userEmail").value("user2@example.com"));

        verify(designComponentService).getAllDesignComponents();
    }

    @Test
    void updateDesignComponent_WithValidData_ShouldReturnUpdatedDesignComponent() throws Exception {
        DesignComponent updateRequest = DesignComponent.builder()
                .imageUrl("https://example.com/updated.png")
                .isPublic(false)
                .build();

        ComponentType componentType = ComponentType.builder()
                .componentTypeId(1)
                .explain("버튼")
                .build();

        DesignComponent updatedComponent = DesignComponent.builder()
                .designComponentId(1)
                .userEmail("user@example.com")
                .imageUrl("https://example.com/updated.png")
                .componentType(componentType)
                .isPublic(false)
                .build();

        when(designComponentService.updateDesignComponent(eq(1), any(DesignComponent.class)))
                .thenReturn(updatedComponent);

        mockMvc.perform(put("/api/design-components/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/updated.png"))
                .andExpect(jsonPath("$.isPublic").value(false));

        verify(designComponentService).updateDesignComponent(eq(1), any(DesignComponent.class));
    }

    @Test
    void updateDesignComponent_WithInvalidId_ShouldReturnNotFound() throws Exception {
        DesignComponent updateRequest = DesignComponent.builder()
                .imageUrl("https://example.com/updated.png")
                .build();

        when(designComponentService.updateDesignComponent(eq(999), any(DesignComponent.class)))
                .thenThrow(new ResourceNotFoundException("DesignComponent not found with id: 999"));

        mockMvc.perform(put("/api/design-components/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("DesignComponent not found with id: 999"));

        verify(designComponentService).updateDesignComponent(eq(999), any(DesignComponent.class));
    }

    @Test
    void deleteDesignComponent_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/design-components/1"))
                .andExpect(status().isNoContent());

        verify(designComponentService).deleteDesignComponent(1);
    }

    @Test
    void deleteDesignComponent_WithInvalidId_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("DesignComponent not found with id: 999"))
                .when(designComponentService).deleteDesignComponent(999);

        mockMvc.perform(delete("/api/design-components/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("DesignComponent not found with id: 999"));

        verify(designComponentService).deleteDesignComponent(999);
    }
}