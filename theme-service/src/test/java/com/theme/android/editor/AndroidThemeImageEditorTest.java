package com.theme.android.editor;

import com.theme.android.dto.AndroidComponentDto;
import com.theme.component.domain.ComponentType;
import com.theme.component.domain.DesignComponent;
import com.theme.component.repository.ComponentTypeRepository;
import com.theme.component.repository.DesignComponentRepository;
import com.theme.utils.ImageUtils;
import com.theme.utils.S3FileManager;
import com.theme.utils.ThemePathManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AndroidThemeImageEditorTest {
    @InjectMocks
    private AndroidThemeImageEditor imageEditor;

    @Mock
    private ImageUtils imageUtil;

    @Spy
    private ThemePathManager themePathManager;

    @Autowired
    private DesignComponentRepository designComponentRepository;

    @Autowired
    private ComponentTypeRepository componentTypeRepository;

    @MockitoBean
    private S3FileManager s3FileManager;

    private DesignComponent designComponent;

    @BeforeEach
    void setUp() {
        String randomValue = UUID.randomUUID().toString();
        ComponentType componentType = componentTypeRepository.save(ComponentType.builder()
                .androidComponentName(randomValue)
                .androidComponentPath(randomValue)
                .iosComponentName(randomValue)
                .iosComponentPath(randomValue)
                .sizeX(100)
                .sizeY(100).build());
        designComponent = designComponentRepository.save(DesignComponent.builder()
                .userEmail(randomValue)
                .imageUrl(randomValue)
                .isPublic(true)
                .componentType(componentType)
                .build());
    }

    @AfterEach
    void tearDown() {
        designComponentRepository.deleteAll();
        componentTypeRepository.deleteAll();
    }

    @Test
    void editImageTest() {
        //given
        String themeId = UUID.randomUUID().toString();
        AndroidComponentDto androidComponentDto = AndroidComponentDto.builder().AndroidComponentName("component name")
                .AndroidComponentPath(designComponent.getComponentType().getAndroidComponentPath())
                .imageUrl(designComponent.getImageUrl())
                .build();
        //stub
        Mockito.doReturn(themePathManager.getThemeDir(themeId)).when(themePathManager).getImagePath(Mockito.any(), Mockito.any());
        Mockito.when(imageUtil.loadImageBytes(Mockito.any())).thenReturn(new byte[100]);
        //when and then
        assertDoesNotThrow(() -> imageEditor.editImage(themeId, androidComponentDto));
    }

    @Test
    void editImagesTest() {
        //given
        String themeId = UUID.randomUUID().toString();
        AndroidComponentDto androidComponentDto = AndroidComponentDto.builder().AndroidComponentName("component name")
                .AndroidComponentPath(designComponent.getComponentType().getAndroidComponentPath())
                .imageUrl(designComponent.getImageUrl())
                .build();
        //stub
        Mockito.doReturn(themePathManager.getThemeDir(themeId)).when(themePathManager).getImagePath(Mockito.any(), Mockito.any());
        Mockito.when(imageUtil.loadImageBytes(Mockito.any())).thenReturn(new byte[100]);
        //when and then
        assertDoesNotThrow(() -> imageEditor.editImages(themeId, List.of(androidComponentDto)));
    }
}