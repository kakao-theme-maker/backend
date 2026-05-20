package com.komentum.theme.android.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.komentum.global.utils.FileManager;
import com.komentum.test.config.EnableTestProfile;
import com.komentum.test.data.ThemeDataGenerator;
import com.komentum.theme.android.dto.AndroidColorDto;
import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.designcomponent.domain.ColorStyle;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.theme.theme.domain.ThemeComponent;
import com.komentum.theme.theme.domain.ThemeImage;
import com.komentum.theme.theme.domain.ThemeStyle;
import com.komentum.global.utils.ImageUtils;
import com.komentum.theme.android.utils.ThemePathManager;
import com.komentum.theme.android.utils.XmlEditor;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@SpringBootTest
@EnableTestProfile
class AndroidThemeMakerTest {

  @Autowired
  private ThemeDataGenerator themeDataGenerator;

  @Autowired
  private FileManager s3FileManager;

  @Autowired
  private XmlEditor xmlEditor;

  @Autowired
  private AndroidThemeMaker androidThemeMaker;

  @Autowired
  private AndroidColorStyleEditor androidColorStyleEditor;

  private ThemeComponent targetComponent;

  @BeforeEach
  void setUp() {
    themeDataGenerator.deleteTestData();
    List<ComponentType> backgroundImages = themeDataGenerator.generateComponentTypes().stream()
        .filter(v -> v.getTypeCode().equals(TypeCode.BACKGROUND_IMAGE)).toList();
    List<ColorStyle> colorStyles = themeDataGenerator.generateColorStyles();
    List<DesignComponent> designComponents = themeDataGenerator.generateDesignComponents(
        backgroundImages.size());
    targetComponent = themeDataGenerator.generateThemeComponents(1, colorStyles, backgroundImages,
        designComponents).get(0);
  }

  @AfterEach
  void tearDown() {
    themeDataGenerator.deleteTestData();
  }

  @Test
  @Transactional
  void makeTheme_success() throws IOException {
    // given
    String generatedThemeFileName = "output-signed.apk";
    byte[] sampleImageBytes = Files.readAllBytes(
        Paths.get("src/test/resources/sample-images/test.png"));
    // stub
    String expectedUrl = UUID.randomUUID().toString();
    when(s3FileManager.downloadFile(anyString())).thenReturn(sampleImageBytes);
    when(s3FileManager.uploadFile(any(), any())).thenReturn(expectedUrl);
    // when
    String savedThemeUrl = androidThemeMaker.makeTheme(targetComponent.getThemeComponentId());
    // then
    Path repackedThemePath = ThemePathManager.getThemeRepackedDir(
        targetComponent.getThemeComponentId().toString());
    // s3 url verification
    assertThat(savedThemeUrl).isEqualTo(expectedUrl);
    // signed apk theme validation
    assertThat(
        Files.exists(Paths.get(repackedThemePath.toString(), generatedThemeFileName))).isTrue();
    // saved images validation
    for (ThemeImage themeImage : targetComponent.getThemeImages()) {
      AndroidComponentDto component = AndroidComponentDto.fromEntity(themeImage);
      Path savedImagePath = ThemePathManager.getImagePath(
          targetComponent.getThemeComponentId().toString(), component);
      BufferedImage savedImage = ImageIO.read(savedImagePath.toFile());
      BufferedImage expectedImage = ImageIO.read(new ByteArrayInputStream(sampleImageBytes));
      assertThat(ImageUtils.compareImages(expectedImage, savedImage)).isTrue();
    }
    // saved color validation
    NodeList savedColorTags = xmlEditor.loadDocument(
            ThemePathManager.getColorSheetPath(
                targetComponent.getThemeComponentId().toString()).toString())
        .getElementsByTagName("color");
    Map<String, String> attrColorMap = IntStream.range(0, savedColorTags.getLength())
        .mapToObj(idx -> (Element) savedColorTags.item(idx))
        .collect(Collectors.toMap(e -> e.getAttribute("name"), Element::getTextContent));
    for (ThemeStyle themeStyle : targetComponent.getThemeStyles()) {
      AndroidColorDto color = AndroidColorDto.fromEntity(themeStyle);
      String actualColor = attrColorMap.get(color.getAttrName());
      assertThat(actualColor).isNotNull();
      assertThat(actualColor).isEqualTo(color.getColor());
    }
    // clean up
    androidThemeMaker.removeLocalThemeFiles(targetComponent.getThemeComponentId());
  }
}