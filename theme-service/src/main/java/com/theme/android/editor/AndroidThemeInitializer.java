package com.theme.android.editor;

import com.theme.utils.ThemePathManager;
import com.theme.utils.DockerProcessRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Component
@RequiredArgsConstructor
public class AndroidThemeInitializer {
    private final DockerProcessRunner dockerProcessRunner;
    /**
     * docker command builder
     * @param inputPath input apk path
     * @param targetPath output path for depacked apk
     * @return docker command array like ["docker", "run", "--rm", "-v", "/input:/input", "-v", "/output:/output", "louie8821/apk_decompiler:test"]
     * */
    public String[] commandBuilder(String inputPath, String targetPath) {
        return new String[]{
                "docker", "run", "--rm",
                "-v", String.format("%s:/input:rw", inputPath),
                "-v", String.format("%s:/output:rw", targetPath),
                "louie8821/apk_decompiler:test",
        };
    }
    /**
     * resolve paths and depack apk on output paths
     * @param inputPath input apk path
     * @param outputPath output path for depacked apk
    * */
    public void depackTheme(Path inputPath, Path outputPath) {
        String[] command = commandBuilder(
                inputPath.toAbsolutePath().toString(),
                outputPath.toAbsolutePath().toString());
        dockerProcessRunner.runDockerProcess(command);
    }
    /**
     * load sample theme
     * @return theme path's parent path
     * */
    public Path copyAndGetSourceThemeDir(String themeId) throws IOException{
        String themeFileName = "sample-theme.apk";
        ClassPathResource resource = new ClassPathResource(themeFileName);
        Path themePath = ThemePathManager.getThemeSourceDir(themeId);
        Files.createDirectories(themePath);
        Path themeFilePath = Paths.get(themePath.toString(), themeFileName);
        try (InputStream is = resource.getInputStream()) {
            Files.copy(is, themeFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return themePath;
    }
    /**
     * initialize the theme on the specific theme path
     * @param themeId theme id
    * */
    public void initTheme(String themeId) throws Exception {
        Path sourceThemePath = copyAndGetSourceThemeDir(themeId);
        Path depackedThemePath = ThemePathManager.getThemeDepackedDir(themeId);
        depackTheme(sourceThemePath, depackedThemePath);
    }
}
