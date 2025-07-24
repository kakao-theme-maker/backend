package com.theme.android.editor;

import com.theme.utils.DockerProcessRunner;
import com.theme.utils.S3FileManager;
import com.theme.utils.ThemePathManager;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AndroidThemeSaverTest {
    @MockitoSpyBean
    private AndroidThemeSaver androidThemeSaver;

    @Autowired
    private ThemePathManager themePathManager;

    @MockitoBean
    private DockerProcessRunner dockerProcessRunner;

    @MockitoBean
    private S3FileManager s3FileManager;

    @Test
    void repackAndSignThemeTest() throws Exception {
        // given
        String themeId = UUID.randomUUID().toString();
        Path inputPath = themePathManager.getThemeDir(themeId);
        Path outputPath = themePathManager.getThemeDir(themeId);
        byte[] expected = new byte[100];
        // stub
        Mockito.doReturn(expected).when(androidThemeSaver).getOutputFileBytes(Paths.get(outputPath.toString(), "output-signed.apk"));
        // when
        byte[] res = androidThemeSaver.repackAndSignTheme(inputPath, outputPath);
        // then
        assertEquals(res, expected);
    }

    @Test
    void repackAndSaveThemeTest() throws Exception {
        // given
        String themeId = UUID.randomUUID().toString();
        Path outputPath = themePathManager.getThemeRepackedDir(themeId);
        byte[] expected = new byte[100];
        // stub
        Mockito.doReturn(expected).when(androidThemeSaver).getOutputFileBytes(Paths.get(outputPath.toString(), "output-signed.apk"));
        // when and then
        assertDoesNotThrow(() -> androidThemeSaver.repackAndSaveTheme(themeId));

    }
}