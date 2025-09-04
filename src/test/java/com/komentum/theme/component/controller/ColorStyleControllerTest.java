package com.komentum.theme.component.controller;

import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.service.ColorStyleService;
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
class ColorStyleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ColorStyleService colorStyleService;

    @InjectMocks
    private ColorStyleController colorStyleController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(colorStyleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createColorStyle_ShouldReturnCreatedColorStyle() throws Exception {
        ColorStyle colorStyle = ColorStyle.builder()
                .explain("배경색 스타일")
                .iosStyleName(".container|background-color")
                .androidStyleName("background_color")
                .build();

        ColorStyle savedStyle = ColorStyle.builder()
                .colorTypeId(1)
                .explain("배경색 스타일")
                .iosStyleName(".container|background-color")
                .androidStyleName("background_color")
                .build();

        when(colorStyleService.createColorStyle(any(ColorStyle.class))).thenReturn(savedStyle);

        mockMvc.perform(post("/api/color-styles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(colorStyle)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colorTypeId").value(1))
                .andExpect(jsonPath("$.explain").value("배경색 스타일"))
                .andExpect(jsonPath("$.iosStyleName").value(".container|background-color"));

        verify(colorStyleService).createColorStyle(any(ColorStyle.class));
    }

    @Test
    void getColorStyleById_WithValidId_ShouldReturnColorStyle() throws Exception {
        ColorStyle colorStyle = ColorStyle.builder()
                .colorTypeId(1)
                .explain("조회 테스트")
                .iosStyleName(".test|color")
                .build();

        when(colorStyleService.getColorStyleById(1)).thenReturn(colorStyle);

        mockMvc.perform(get("/api/color-styles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.colorTypeId").value(1))
                .andExpect(jsonPath("$.explain").value("조회 테스트"));

        verify(colorStyleService).getColorStyleById(1);
    }

    @Test
    void getColorStyleById_WithInvalidId_ShouldReturnNotFound() throws Exception {
        when(colorStyleService.getColorStyleById(999))
                .thenThrow(new ResourceNotFoundException("ColorStyle not found with id: 999"));

        mockMvc.perform(get("/api/color-styles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ColorStyle not found with id: 999"));

        verify(colorStyleService).getColorStyleById(999);
    }

    @Test
    void getAllColorStyles_ShouldReturnColorStyleList() throws Exception {
        List<ColorStyle> colorStyles = Arrays.asList(
                ColorStyle.builder()
                        .colorTypeId(1)
                        .explain("배경색")
                        .build(),
                ColorStyle.builder()
                        .colorTypeId(2)
                        .explain("텍스트색")
                        .build()
        );

        when(colorStyleService.getAllColorStyles()).thenReturn(colorStyles);

        mockMvc.perform(get("/api/color-styles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].explain").value("배경색"))
                .andExpect(jsonPath("$[1].explain").value("텍스트색"));

        verify(colorStyleService).getAllColorStyles();
    }

    @Test
    void updateColorStyle_WithValidData_ShouldReturnUpdatedColorStyle() throws Exception {
        ColorStyle updateRequest = ColorStyle.builder()
                .explain("수정된 설명")
                .build();

        ColorStyle updatedStyle = ColorStyle.builder()
                .colorTypeId(1)
                .explain("수정된 설명")
                .iosStyleName(".container|background-color")
                .build();

        when(colorStyleService.updateColorStyle(eq(1), any(ColorStyle.class))).thenReturn(updatedStyle);

        mockMvc.perform(put("/api/color-styles/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.explain").value("수정된 설명"))
                .andExpect(jsonPath("$.iosStyleName").value(".container|background-color"));

        verify(colorStyleService).updateColorStyle(eq(1), any(ColorStyle.class));
    }

    @Test
    void updateColorStyle_WithInvalidId_ShouldReturnNotFound() throws Exception {
        ColorStyle updateRequest = ColorStyle.builder()
                .explain("수정된 설명")
                .build();

        when(colorStyleService.updateColorStyle(eq(999), any(ColorStyle.class)))
                .thenThrow(new ResourceNotFoundException("ColorStyle not found with id: 999"));

        mockMvc.perform(put("/api/color-styles/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ColorStyle not found with id: 999"));

        verify(colorStyleService).updateColorStyle(eq(999), any(ColorStyle.class));
    }

    @Test
    void deleteColorStyle_WithValidId_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/color-styles/1"))
                .andExpect(status().isNoContent());

        verify(colorStyleService).deleteColorStyle(1);
    }

    @Test
    void deleteColorStyle_WithInvalidId_ShouldReturnNotFound() throws Exception {
        doThrow(new ResourceNotFoundException("ColorStyle not found with id: 999"))
                .when(colorStyleService).deleteColorStyle(999);

        mockMvc.perform(delete("/api/color-styles/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("ColorStyle not found with id: 999"));

        verify(colorStyleService).deleteColorStyle(999);
    }
}