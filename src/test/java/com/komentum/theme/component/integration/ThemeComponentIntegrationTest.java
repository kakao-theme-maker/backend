package com.komentum.theme.component.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.komentum.theme.component.domain.ColorStyle;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateColorStyleRequest;
import com.komentum.theme.component.dto.CreateComponentTypeRequest;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.UpdateColorStyleRequest;
import com.komentum.theme.component.dto.UpdateComponentTypeRequest;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import com.komentum.theme.component.enums.Platform;
import com.komentum.theme.component.repository.ColorStyleRepository;
import com.komentum.theme.component.repository.ComponentTypeRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.theme.utils.S3FileManager;
import com.komentum.global.security.SecurityProperties;
import com.komentum.user.redis.RedisSingleDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("Theme Component 통합 테스트")
class ThemeComponentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ColorStyleRepository colorStyleRepository;

    @Autowired
    private ComponentTypeRepository componentTypeRepository;

    @Autowired
    private DesignComponentRepository designComponentRepository;

    @MockBean
    private S3FileManager s3FileManager;

    @MockBean
    private RedisSingleDataService redisSingleDataService;

    @BeforeEach
    void setUp() {
        colorStyleRepository.deleteAll();
        componentTypeRepository.deleteAll();
        designComponentRepository.deleteAll();
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

    @Nested
    @DisplayName("DesignComponent CRUD 테스트")
    class DesignComponentTests {

        @Test
        @DisplayName("DesignComponent 생성 테스트")
        void createDesignComponent() throws Exception {
            // Given
            CreateDesignComponentRequest request = CreateDesignComponentRequest.builder()
                .userEmail("test@example.com")
                .imageUrl("https://example.com/image.png")
                .isPublic(true)
                .build();

            // When & Then
            mockMvc.perform(post("/api/design-components")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.imageUrl").value("https://example.com/image.png"))
                .andExpect(jsonPath("$.isPublic").value(true));

            assertThat(designComponentRepository.count()).isEqualTo(1);
        }

        @Test
        @DisplayName("DesignComponent 조회 테스트")
        void getDesignComponent() throws Exception {
            // Given
            DesignComponent savedComponent = designComponentRepository.save(DesignComponent.builder()
                .userEmail("test@example.com")
                .imageUrl("https://test.com/image.jpg")
                .isPublic(false)
                .build());

            // When & Then
            mockMvc.perform(get("/api/design-components/{id}", savedComponent.getDesignComponentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.designComponentId").value(savedComponent.getDesignComponentId()))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"));
        }

        @Test
        @DisplayName("DesignComponent 전체 조회 테스트")
        void getAllDesignComponents() throws Exception {
            // Given
            designComponentRepository.save(DesignComponent.builder()
                .userEmail("user1@test.com")
                .imageUrl("https://test1.com/image.jpg")
                .isPublic(true)
                .build());

            designComponentRepository.save(DesignComponent.builder()
                .userEmail("user2@test.com")
                .imageUrl("https://test2.com/image.jpg")
                .isPublic(false)
                .build());

            // When & Then
            mockMvc.perform(get("/api/design-components"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
        }

        @Test
        @DisplayName("사용자별 DesignComponent 조회 테스트")
        void getDesignComponentsByUserEmail() throws Exception {
            // Given
            String userEmail = "user@test.com";
            designComponentRepository.save(DesignComponent.builder()
                .userEmail(userEmail)
                .imageUrl("https://test1.com/image.jpg")
                .isPublic(true)
                .build());

            designComponentRepository.save(DesignComponent.builder()
                .userEmail("other@test.com")
                .imageUrl("https://test2.com/image.jpg")
                .isPublic(true)
                .build());

            // When & Then
            mockMvc.perform(get("/api/design-components/user/{userEmail}", userEmail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value(userEmail));
        }

        @Test
        @DisplayName("공개 DesignComponent 조회 테스트")
        void getPublicDesignComponents() throws Exception {
            // Given
            designComponentRepository.save(DesignComponent.builder()
                .userEmail("user1@test.com")
                .imageUrl("https://test1.com/image.jpg")
                .isPublic(true)
                .build());

            designComponentRepository.save(DesignComponent.builder()
                .userEmail("user2@test.com")
                .imageUrl("https://test2.com/image.jpg")
                .isPublic(false)
                .build());

            // When & Then
            mockMvc.perform(get("/api/design-components/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].isPublic").value(true));
        }

        @Test
        @DisplayName("DesignComponent 수정 테스트")
        void updateDesignComponent() throws Exception {
            // Given
            DesignComponent savedComponent = designComponentRepository.save(DesignComponent.builder()
                .userEmail("original@test.com")
                .imageUrl("https://original.com/image.jpg")
                .isPublic(false)
                .build());

            UpdateDesignComponentRequest updateRequest = UpdateDesignComponentRequest.builder()
                .userEmail("updated@test.com")
                .imageUrl("https://updated.com/image.jpg")
                .isPublic(true)
                .build();

            // When & Then
            mockMvc.perform(put("/api/design-components/{id}", savedComponent.getDesignComponentId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value("updated@test.com"))
                .andExpect(jsonPath("$.isPublic").value(true));
        }

        @Test
        @DisplayName("DesignComponent 삭제 테스트")
        void deleteDesignComponent() throws Exception {
            // Given
            DesignComponent savedComponent = designComponentRepository.save(DesignComponent.builder()
                .userEmail("delete@test.com")
                .imageUrl("https://delete.com/image.jpg")
                .isPublic(false)
                .build());

            // When & Then
            mockMvc.perform(delete("/api/design-components/{id}", savedComponent.getDesignComponentId()))
                .andExpect(status().isNoContent());

            assertThat(designComponentRepository.existsById(savedComponent.getDesignComponentId())).isFalse();
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationScenarioTests {

        @Test
        @DisplayName("전체 컴포넌트 생성 및 연동 테스트")
        void createAndLinkAllComponents() throws Exception {
            // ColorStyle 생성
            CreateColorStyleRequest colorStyleRequest = CreateColorStyleRequest.builder()
                .explain("통합 테스트 색상")
                .platform(Platform.ANDROID)
                .styleSheetPath("/integration.css")
                .styleElementName("integrationColor")
                .stylePropsName("color")
                .build();

            mockMvc.perform(post("/api/color-styles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(colorStyleRequest)))
                .andExpect(status().isOk());

            // ComponentType 생성
            CreateComponentTypeRequest componentTypeRequest = CreateComponentTypeRequest.builder()
                .explain("통합 테스트 컴포넌트")
                .platform(Platform.ANDROID)
                .componentPath("/integration")
                .componentName("IntegrationComponent")
                .sizeX(300)
                .sizeY(200)
                .build();

            mockMvc.perform(post("/api/component-types")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(componentTypeRequest)))
                .andExpect(status().isOk());

            // DesignComponent 생성
            CreateDesignComponentRequest designComponentRequest = CreateDesignComponentRequest.builder()
                .userEmail("integration@test.com")
                .imageUrl("https://integration.com/image.png")
                .isPublic(true)
                .build();

            mockMvc.perform(post("/api/design-components")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(designComponentRequest)))
                .andExpect(status().isOk());

            // 데이터베이스 검증
            assertThat(colorStyleRepository.count()).isEqualTo(1);
            assertThat(componentTypeRepository.count()).isEqualTo(1);
            assertThat(designComponentRepository.count()).isEqualTo(1);
        }
    }
}