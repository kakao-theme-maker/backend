package com.komentum.theme.android.editor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.global.enums.FileExtension;
import com.komentum.global.utils.FileManager;
import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.android.utils.ThemePathManager;
import com.komentum.theme.core.domain.ImageInset;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class AndroidThemeImageEditorTest {

  private static final String BUILD_IDENTIFIER = "build";
  private static final int BLACK = Color.BLACK.getRGB();

  @TempDir
  Path tempDir;
  private FileManager fileManager;
  private AndroidThemeImageEditor editor;

  @BeforeEach
  void setUp() {
    fileManager = Mockito.mock(FileManager.class);
    ThemePathManager themePathManager = Mockito.mock(ThemePathManager.class);
    when(themePathManager.getAndroidThemeImagePath(anyString(), any(AndroidComponentDto.class)))
        .thenAnswer(invocation -> tempDir.resolve(
            invocation.<AndroidComponentDto>getArgument(1).getImageFilePath()));
    editor = new AndroidThemeImageEditor(fileManager, themePathManager);
  }

  @AfterEach
  void tearDown() {
    editor.shutdown();
  }

  @Test
  @DisplayName("일반 이미지는 경로별 스펙 크기에 정확히 맞춰지고, 비율이 다르면 투명 여백으로 채워진다")
  void editImages_resizeIconToEachSpecSize() throws Exception {
    // given
    givenSourceImage("icon.png", 300, 150);
    AndroidComponentDto mdpi = component(TypeCode.COMMON_ICO_THEME, "icon.png",
        "res/mipmap-mdpi/ic_launcher.png", 48, 48, FileExtension.PNG, null);
    AndroidComponentDto xxxhdpi = component(TypeCode.COMMON_ICO_THEME, "icon.png",
        "res/mipmap-xxxhdpi/ic_launcher.png", 192, 192, FileExtension.PNG, null);
    // when
    editor.editImages("1", BUILD_IDENTIFIER, List.of(mdpi, xxxhdpi));
    // then
    BufferedImage mdpiImage = readResult(mdpi);
    BufferedImage xxxhdpiImage = readResult(xxxhdpi);
    assertThat(mdpiImage.getWidth()).isEqualTo(48);
    assertThat(mdpiImage.getHeight()).isEqualTo(48);
    assertThat(xxxhdpiImage.getWidth()).isEqualTo(192);
    assertThat(xxxhdpiImage.getHeight()).isEqualTo(192);
    assertThat(alpha(mdpiImage, 24, 0)).isZero();
    assertThat(alpha(mdpiImage, 24, 24)).isEqualTo(255);
  }

  @Test
  @DisplayName("배경 이미지는 자르지 않고 비율을 유지한 채 스펙을 덮는 크기까지만 축소된다")
  void editImages_scaleDownBackgroundWithoutCrop() throws Exception {
    // given
    givenSourceImage("background.png", 400, 300);
    AndroidComponentDto background = component(TypeCode.CHAT_ROOM_BACKGROUND_IMAGE,
        "background.png", "theme/drawable-xxhdpi/theme_chatroom_background_image.png", 144, 288,
        FileExtension.PNG, null);
    // when
    editor.editImages("1", BUILD_IDENTIFIER, List.of(background));
    // then
    BufferedImage result = readResult(background);
    assertThat(result.getWidth()).isEqualTo(384);
    assertThat(result.getHeight()).isEqualTo(288);
  }

  @Test
  @DisplayName("스펙보다 작은 배경 이미지는 확대하지 않고 원본 크기를 유지한다")
  void editImages_keepSmallBackground() throws Exception {
    // given
    givenSourceImage("background.png", 100, 100);
    AndroidComponentDto background = component(TypeCode.PASSCODE_BACKGROUND_IMAGE,
        "background.png", "theme/drawable-xxhdpi/theme_passcode_background_image.png", 144, 144,
        FileExtension.PNG, null);
    // when
    editor.editImages("1", BUILD_IDENTIFIER, List.of(background));
    // then
    BufferedImage result = readResult(background);
    assertThat(result.getWidth()).isEqualTo(100);
    assertThat(result.getHeight()).isEqualTo(100);
  }

  @Test
  @DisplayName("inset이 없는 9-patch 배경은 축소 후 마커 없는 1px 테두리가 추가된다")
  void editImages_scaleDownNinePatchBackgroundWithoutMarker() throws Exception {
    // given
    givenSourceImage("tabbar.png", 288, 42);
    AndroidComponentDto tabbar = component(TypeCode.TABBAR_STYLE_BACKGROUND_IMAGE, "tabbar.png",
        "theme/drawable-xxhdpi/theme_maintab_cell_image.9.png", 144, 21,
        FileExtension.NINE_PATCH_PNG, null);
    // when
    editor.editImages("1", BUILD_IDENTIFIER, List.of(tabbar));
    // then
    BufferedImage result = readResult(tabbar);
    assertThat(result.getWidth()).isEqualTo(146);
    assertThat(result.getHeight()).isEqualTo(23);
    assertThat(alpha(result, 73, 0)).isZero();
    assertThat(alpha(result, 0, 11)).isZero();
  }

  @Test
  @DisplayName("스펙보다 큰 말풍선은 비율을 유지해 축소되고, 9-patch 마커는 같은 비율로 변환된 inset 위치에 그려진다")
  void editImages_scaleDownBubbleWithScaledInset() throws Exception {
    // given
    givenSourceImage("bubble.png", 600, 560);
    ImageInset inset = new ImageInset(300, 280, 30, 40, 30, 40);
    AndroidComponentDto bubble = component(TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE,
        "bubble.png", "theme/drawable-xxhdpi/theme_chatroom_bubble_me_01_image.9.png", 124, 114,
        FileExtension.NINE_PATCH_PNG, inset);
    // when
    editor.editImages("1", BUILD_IDENTIFIER, List.of(bubble));
    // then ( 122x114로 축소, stretchX=61, left=right=8 )
    BufferedImage result = readResult(bubble);
    assertThat(result.getWidth()).isEqualTo(124);
    assertThat(result.getHeight()).isEqualTo(116);
    assertThat(result.getRGB(61, 0)).isEqualTo(BLACK);
    assertThat(result.getRGB(60, 0)).isNotEqualTo(BLACK);
    assertThat(result.getRGB(8, 115)).isEqualTo(BLACK);
    assertThat(result.getRGB(7, 115)).isNotEqualTo(BLACK);
    // DB에 저장된 원본 inset은 변경되지 않는다
    assertThat(inset.getStretchX()).isEqualTo(300);
  }

  @Test
  @DisplayName("스펙보다 작은 말풍선은 확대하지 않고 원본 크기와 원본 inset으로 9-patch 변환된다")
  void editImages_keepSmallBubble() throws Exception {
    // given
    givenSourceImage("bubble.png", 100, 90);
    ImageInset inset = new ImageInset(50, 45, 15, 15, 15, 15);
    AndroidComponentDto bubble = component(TypeCode.MESSAGE_CELL_STYLE_SEND_BACKGROUND_IMAGE,
        "bubble.png", "theme/drawable-xxhdpi/theme_chatroom_bubble_me_01_image.9.png", 124, 114,
        FileExtension.NINE_PATCH_PNG, inset);
    // when
    editor.editImages("1", BUILD_IDENTIFIER, List.of(bubble));
    // then
    BufferedImage result = readResult(bubble);
    assertThat(result.getWidth()).isEqualTo(102);
    assertThat(result.getHeight()).isEqualTo(92);
    assertThat(result.getRGB(50, 0)).isEqualTo(BLACK);
    assertThat(result.getRGB(49, 0)).isNotEqualTo(BLACK);
  }

  private void givenSourceImage(String fileName, int width, int height) throws IOException {
    byte[] bytes = createImageBytes(width, height);
    when(fileManager.download(fileName)).thenAnswer(
        invocation -> new ByteArrayInputStream(bytes));
  }

  private AndroidComponentDto component(TypeCode typeCode, String fileName, String path,
      int width, int height, FileExtension fileExtension, ImageInset inset) {
    return AndroidComponentDto.builder()
        .typeCode(typeCode)
        .imageFileName(fileName)
        .imageFilePath(path)
        .sizeX(width)
        .sizeY(height)
        .fileExtension(fileExtension)
        .imageInset(inset)
        .build();
  }

  private BufferedImage readResult(AndroidComponentDto component) throws IOException {
    return ImageIO.read(tempDir.resolve(component.getImageFilePath()).toFile());
  }

  private int alpha(BufferedImage image, int x, int y) {
    return new Color(image.getRGB(x, y), true).getAlpha();
  }

  private byte[] createImageBytes(int width, int height) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(Color.WHITE);
    graphics.fillRect(0, 0, width, height);
    graphics.dispose();
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(image, "png", baos);
      return baos.toByteArray();
    }
  }
}
