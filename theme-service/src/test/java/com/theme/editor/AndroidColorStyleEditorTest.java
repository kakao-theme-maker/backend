package com.theme.editor;

import com.theme.dto.editor.AndroidColorDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AndroidColorStyleEditorTest {
    private AndroidColorStyleEditor androidColorStyleEditor;

    Path testFilePath;

    @BeforeEach
    void setup() throws IOException {
        Path original = Paths.get("src/test/resources/colors.xml");
        testFilePath = Files.copy(original, Paths.get("build/tmp/colors.xml"), StandardCopyOption.REPLACE_EXISTING);
        androidColorStyleEditor = new AndroidColorStyleEditor();
    }

    @AfterEach
    void teardown() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Test
    @DisplayName("색상 수정 성공 테스트")
    void editColor_success() {
        // given
        String themeId = "1";
        String attrName = "theme_header_color";
        AndroidColorDto androidColorDto = AndroidColorDto.builder()
                .color("new color")
                .attrName(attrName)
                .build();
        // stub
        try (MockedStatic<AndroidThemePathManager> mockedStatic = Mockito.mockStatic(AndroidThemePathManager.class)) {
            mockedStatic.when(() -> AndroidThemePathManager.getColorSheetPath(themeId))
                    .thenReturn(testFilePath);
            // when
            androidColorStyleEditor.editColor(themeId, androidColorDto);
            // then
            Document document = androidColorStyleEditor.loadDocument(testFilePath.toString());
            NodeList colors = document.getElementsByTagName("color");
            Element colorElement = androidColorStyleEditor.getElementByAttribute(colors, attrName);
            assert colorElement != null;
            assert colorElement.getTextContent().equals(androidColorDto.getColor());
        }
    }

    @Test
    @DisplayName("색상 일괄 수정 성공 테스트")
    void editColors_success() {
        // given
        String themeId = "1";
        String attrName1 = "theme_header_color";
        String attrName2 = "theme_section_title_color";
        AndroidColorDto androidColorDto1 = AndroidColorDto.builder()
                .color("new color")
                .attrName(attrName1)
                .build();
        AndroidColorDto androidColorDto2 = AndroidColorDto.builder()
                .color("new color")
                .attrName(attrName2)
                .build();
        List <AndroidColorDto> colorDtoList = Arrays.asList(androidColorDto1, androidColorDto2);
        // stub
        try (MockedStatic<AndroidThemePathManager> mockedStatic = Mockito.mockStatic(AndroidThemePathManager.class)) {
            mockedStatic.when(() -> AndroidThemePathManager.getColorSheetPath(themeId))
                    .thenReturn(testFilePath);
            // when
            androidColorStyleEditor.editColors(themeId, colorDtoList);
            // then
            for (AndroidColorDto androidColorDto : colorDtoList) {
                Document document = androidColorStyleEditor.loadDocument(testFilePath.toString());
                NodeList colors = document.getElementsByTagName("color");
                String attrName = androidColorDto.getAttrName();
                Element colorElement = androidColorStyleEditor.getElementByAttribute(colors, attrName);
                assert colorElement != null;
                assert colorElement.getTextContent().equals(androidColorDto.getColor());
            }
        }
    }
}