package com.theme.utils;

import com.theme.android.dto.AndroidComponentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RequiredArgsConstructor
public class ThemePathManager {
    /**
     * get a root directory path of the theme
    * */
    public String getBasePath(){
        String os = System.getProperty("os.name");
        if(os.toLowerCase().startsWith("win")) {
            return "C:\\tmp";
        }else {
            return "/tmp";
        }
    }
    /**
     * get a specific theme's directory
    * */
    public Path getThemeDir(String themeId) {
        Path basePath = Path.of(getBasePath());
        return Paths.get(basePath.toAbsolutePath().toString(), "sheet", "android", themeId);
    }
    /**
     * get a specific theme's sample source apk directory
     * */
    public Path getThemeSourceDir(String themeId){
        return Paths.get(getThemeDir(themeId).toString(), "source");
    }
    /**
     * get a specific theme's depacked theme directory
    * */
    public Path getThemeDepackedDir(String themeId){
        return Paths.get(getThemeDir(themeId).toString(), "depack");
    }
    /**
     * get a specific theme's repackaged theme directory
    * */
    public Path getThemeRepackedDir(String themeId){
        return Paths.get(getThemeDir(themeId).toString(), "repack");
    }
    /**
     * get a specific theme's depacked image path
    * */
    public Path getImagePath(String themeId, AndroidComponentDto component) {
        return Paths.get(getThemeDepackedDir(themeId).toString(), component.getAndroidComponentPath());
    }
    /**
     * get a specific theme's depacked color sheet path
    * */
    public Path getColorSheetPath(String themeId){
        return Paths.get(getThemeDepackedDir(themeId).toString(), "res", "values", "colors.xml");
    }
}
