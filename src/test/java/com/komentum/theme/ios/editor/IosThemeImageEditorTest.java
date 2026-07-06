package com.komentum.theme.ios.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.enums.FileExtension;
import com.komentum.global.utils.FileManager;
import com.komentum.theme.core.domain.ThemeImage;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class IosThemeImageEditorTest {

  @TempDir
  Path tempDir;

  @Test
  void editImages_createsIosVariantsWithMappedSize() throws Exception {
    // given
    Files.createDirectories(tempDir.resolve("Images"));
    FileManager fileManager = Mockito.mock(FileManager.class);
    IosThemeImageEditor editor = new IosThemeImageEditor(fileManager);
    byte[] imageBytes = createImageBytes(20, 10);
    when(fileManager.convertUrlToFileName("https://cdn.example.com/source.png"))
        .thenReturn("source.png");
    when(fileManager.downloadFile("source.png")).thenReturn(imageBytes);

    ComponentType componentType = ComponentType.builder()
        .componentTypeId(1)
        .typeCode(TypeCode.TABBAR_STYLE_FRIENDS_NORMAL_ICON_IMAGE)
        .name("friends")
        .build();
    ThemeImage themeImage = ThemeImage.builder()
        .componentType(componentType)
        .designComponent(DesignComponent.builder()
            .designComponentId(1)
            .imageUrl("https://cdn.example.com/source.png")
            .build())
        .build();
    PlatformComponentType twoX = PlatformComponentType.builder()
        .platform(Platform.IOS)
        .componentType(componentType)
        .path("maintabIcoFriends@2x.png")
        .width(76)
        .height(76)
        .fileExtension(FileExtension.PNG)
        .code("iosFriends2x")
        .build();
    PlatformComponentType threeX = PlatformComponentType.builder()
        .platform(Platform.IOS)
        .componentType(componentType)
        .path("maintabIcoFriends@3x.png")
        .width(114)
        .height(114)
        .fileExtension(FileExtension.PNG)
        .code("iosFriends3x")
        .build();

    // when
    editor.editImages(tempDir, List.of(themeImage), List.of(twoX, threeX));

    // then
    BufferedImage twoXImage = ImageIO.read(tempDir.resolve("Images/maintabIcoFriends@2x.png").toFile());
    BufferedImage threeXImage = ImageIO.read(tempDir.resolve("Images/maintabIcoFriends@3x.png").toFile());
    assertThat(twoXImage.getWidth()).isEqualTo(76);
    assertThat(twoXImage.getHeight()).isEqualTo(76);
    assertThat(threeXImage.getWidth()).isEqualTo(114);
    assertThat(threeXImage.getHeight()).isEqualTo(114);
  }

  @Test
  void editImages_keepsTemplateImageWhenThemeImageUrlIsMissing() throws Exception {
    // given
    Files.createDirectories(tempDir.resolve("Images"));
    FileManager fileManager = Mockito.mock(FileManager.class);
    IosThemeImageEditor editor = new IosThemeImageEditor(fileManager);
    ComponentType componentType = ComponentType.builder()
        .componentTypeId(1)
        .typeCode(TypeCode.TABBAR_STYLE_FRIENDS_NORMAL_ICON_IMAGE)
        .name("friends")
        .build();
    ThemeImage themeImage = ThemeImage.builder()
        .componentType(componentType)
        .designComponent(DesignComponent.builder()
            .designComponentId(1)
            .imageUrl(null)
            .build())
        .build();
    PlatformComponentType platformComponentType = PlatformComponentType.builder()
        .platform(Platform.IOS)
        .componentType(componentType)
        .path("maintabIcoFriends@2x.png")
        .width(76)
        .height(76)
        .fileExtension(FileExtension.PNG)
        .code("iosFriends2x")
        .build();

    // when
    editor.editImages(tempDir, List.of(themeImage), List.of(platformComponentType));

    // then
    assertThat(tempDir.resolve("Images/maintabIcoFriends@2x.png")).doesNotExist();
    verify(fileManager, never()).convertUrlToFileName(Mockito.any());
  }

  @Test
  void editImages_throwsWhenIosPlatformComponentMappingIsMissing() {
    // given
    FileManager fileManager = Mockito.mock(FileManager.class);
    IosThemeImageEditor editor = new IosThemeImageEditor(fileManager);

    // when & then
    assertThatThrownBy(() -> editor.editImages(tempDir, List.of(), List.of()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("iOS platform component type seed is required");
    verify(fileManager, never()).convertUrlToFileName(Mockito.any());
  }

  private byte[] createImageBytes(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(Color.RED);
    graphics.fillRect(0, 0, width, height);
    graphics.dispose();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(image, "png", outputStream);
    return outputStream.toByteArray();
  }
}
