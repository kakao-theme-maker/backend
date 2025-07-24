package com.theme.android.editor;

import com.theme.android.dto.AndroidComponentDto;
import com.theme.utils.ThemePathManager;
import com.theme.utils.ImageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AndroidThemeImageEditor {
    private final ImageUtils imageUtils;
    private final ThemePathManager themePathManager;

    /**
     * edit an image on the specific theme path
     * @param themeId theme id
     * @param component theme's component info
    * */
    public void editImage(String themeId, AndroidComponentDto component) throws IOException {
        Path imagePath = themePathManager.getImagePath(themeId, component);
        try(FileOutputStream fos = new FileOutputStream(imagePath.toFile())){
            byte[] imageBytes = imageUtils.loadImageBytes(component.getImageUrl());
            if(imageBytes == null) {
                throw new IOException("AndroidThemeImageEditor.editImage : Image bytes is null");
            }
            fos.write(imageBytes);
            fos.flush();
        }
    }

    /**
     * edit all images on the specific theme path by the component list
     * @param themeId theme id
     * @param components theme's component info list
    * */
    public void editImages(String themeId, List<AndroidComponentDto> components) throws IOException {
        for(AndroidComponentDto component : components) {
            editImage(themeId, component);
        }
    }
}
