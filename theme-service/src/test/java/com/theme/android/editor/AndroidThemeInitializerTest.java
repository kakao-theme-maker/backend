package com.theme.android.editor;

import com.theme.utils.DockerProcessRunner;
import com.theme.utils.S3FileManager;
import com.theme.utils.ThemePathManager;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AndroidThemeInitializerTest {
    @Autowired
    private AndroidThemeInitializer androidThemeInitializer;

    @MockitoBean
    private DockerProcessRunner dockerProcessRunner;

    @MockitoBean
    private S3FileManager s3FileManager;

    @Test
    void copyAndGetSourceThemeDir() {
        String themeId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> androidThemeInitializer.initTheme(themeId));
    }

    @Test
    void initTheme() {
        String themeId = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> androidThemeInitializer.initTheme(themeId));
    }
}