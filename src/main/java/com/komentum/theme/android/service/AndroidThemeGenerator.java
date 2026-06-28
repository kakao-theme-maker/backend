package com.komentum.theme.android.service;

import com.komentum.designcomponent.domain.PlatformColorStyle;
import com.komentum.designcomponent.domain.PlatformComponentType;
import com.komentum.designcomponent.enums.Platform;
import com.komentum.designcomponent.enums.StyleCode;
import com.komentum.designcomponent.enums.TypeCode;
import com.komentum.designcomponent.repository.PlatformColorStyleRepository;
import com.komentum.designcomponent.repository.PlatformComponentTypeRepository;
import com.komentum.global.utils.CompressUtils;
import com.komentum.global.utils.CustomFileUtils;
import com.komentum.global.utils.FileManager;
import com.komentum.theme.android.dto.AndroidColorDto;
import com.komentum.theme.android.dto.AndroidComponentDto;
import com.komentum.theme.android.editor.AndroidColorStyleEditor;
import com.komentum.theme.android.editor.AndroidMetaDataEditor;
import com.komentum.theme.android.editor.AndroidThemeImageEditor;
import com.komentum.theme.android.properties.AndroidSigningProperties;
import com.komentum.theme.android.utils.DockerProcessRunner;
import com.komentum.theme.android.utils.ThemePathManager;
import com.komentum.theme.core.domain.ThemeComponent;
import com.komentum.theme.core.domain.ThemeImage;
import com.komentum.theme.core.domain.ThemeStyle;
import com.komentum.theme.core.repository.ThemeImageRepository;
import com.komentum.theme.core.repository.ThemeStyleRepository;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(AndroidSigningProperties.class)
public class AndroidThemeGenerator {

  private final ThemeImageRepository themeImageRepository;
  private final PlatformComponentTypeRepository platformComponentTypeRepository;
  private final PlatformColorStyleRepository platformColorStyleRepository;
  private final ThemeStyleRepository themeStyleRepository;
  private final AndroidSigningProperties signingProperties;
  private final DockerProcessRunner dockerProcessRunner;
  private final AndroidThemeImageEditor androidThemeImageEditor;
  private final AndroidColorStyleEditor androidColorStyleEditor;
  private final AndroidMetaDataEditor androidMetaDataEditor;
  private final FileManager fileManager;

  private static final String SAMPLE_THEME_PATH = "themeSample/android/sampleTheme.zip";
  public static final String DOCKER_THEME_DIRECTORY_NAME = "source";

  /**
   * Android 테마를 생성하여 APK를 빌드한 뒤 업로드하고, 업로드된 APK의 URL을 반환한다.
   * 작업이 종료되면 성공 여부와 관계없이 임시 작업 디렉토리를 정리한다.
   *
   * @param themeComponent 테마 Entity
   * @return 업로드된 APK의 URL
   */
  public String createAndSaveTheme(ThemeComponent themeComponent) {
    Integer themeId = themeComponent.getThemeComponentId();
    try {
      // 1. 임시 테마를 임시 디렉토리에 만든다
      Path sourceThemePath = prepareSourceTheme(
          ThemePathManager.getThemeSourceDir(themeId.toString()));
      // 2. 임시 테마의 요소들을 수정한다
      applyThemeImages(themeId);
      applyThemeStyles(themeId);
      // 3. 임시 테마 메타 데이터 수정
      androidMetaDataEditor.editThemeName(themeId.toString(), themeComponent.getThemeName());
      // 3. 리소스가 수정된 임시 테마를 Docker 호스트 볼륨 마운트를 통해 빌드한다
      String[] command = createDockerCommandForApkBuild(sourceThemePath,
          themeComponent.getThemeCode());
      dockerProcessRunner.runDockerProcess(command);
      // 4. Docker 호스트 볼륨 마운트를 통해 빌드한 결과물을 FileManager로 업로드 및 반환한다
      return uploadTheme(themeId);
    } finally {
      CustomFileUtils.deleteDirectorySilently(ThemePathManager.getThemeDir(themeId.toString()));
    }
  }

  // ======================================================
  // Internal Theme Generation Pipeline Steps
  // ======================================================

  /**
   * 1. 기본 테마 압축 파일을 지정된 디렉토리에 압축 해제한다.
   *
   * @param destination 기본 테마를 생성할 디렉토리
   * @return 압축 해제된 테마의 루트 디렉토리
   */
  private Path prepareSourceTheme(Path destination) {
    try (InputStream is = new ClassPathResource(SAMPLE_THEME_PATH).getInputStream()) {
      CompressUtils.unzip(is, destination);
      return destination;
    } catch (IOException e) {
      log.error("[AndroidThemeGenerator] Failed to copy and unzip sample theme zip.", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * 2-1. DB에 저장된 테마 이미지 정보를 기반으로 테마 리소스 이미지를 교체한다.
   *
   * @param themeId 테마 식별자
   */
  private void applyThemeImages(Integer themeId) {
    // themeImage, platformComponentType 조회
    List<ThemeImage> themeImageList = themeImageRepository.fetchJoinByThemeComponentId(themeId);
    Map<TypeCode, List<PlatformComponentType>> platformComponentTypeMap = platformComponentTypeRepository
        .fetchJoinAllByPlatform(Platform.ANDROID)
        .stream().collect(Collectors.groupingBy(
            v -> v.getComponentType().getTypeCode()
        ));
    // 이미지 저장용 DTO 목록 조회
    List<AndroidComponentDto> androidImageList = new ArrayList<>();
    for (ThemeImage themeImage : themeImageList) {
      TypeCode targetTypeCode = themeImage.getComponentType().getTypeCode();
      List<PlatformComponentType> platformComponentTypes = platformComponentTypeMap.get(
          targetTypeCode);
      if (platformComponentTypes == null || platformComponentTypes.isEmpty()) {
        throw new RuntimeException(
            "platformComponentType is null with typeCode :" + targetTypeCode.name());
      }
      for (PlatformComponentType pc : platformComponentTypes) {
        String imageUrl = themeImage.getDesignComponent().getImageUrl();
        androidImageList.add(AndroidComponentDto.fromEntity(pc, imageUrl));
      }
    }
    // 이미지 파일을 테마에 저장
    androidThemeImageEditor.editImages(themeId.toString(), androidImageList);
  }

  /**
   * 2-2. DB에 저장된 테마 색상 정보를 기반으로 테마 리소스의 색상을 변경한다.
   *
   * @param themeId 테마 식별자
   */
  private void applyThemeStyles(Integer themeId) {
    // themeImage, platformColorStyle 조회
    List<ThemeStyle> themeStyleList = themeStyleRepository.fetchJoinByThemeComponentId(themeId);
    Map<StyleCode, List<PlatformColorStyle>> platformColorStyleMap = platformColorStyleRepository
        .fetchJoinAllByPlatform(Platform.ANDROID)
        .stream().collect(Collectors.groupingBy(
            v -> v.getColorStyle().getStyleCode()
        ));
    // 색상 수정용 DTO 생성
    List<AndroidColorDto> androidColorList = new ArrayList<>();
    for (ThemeStyle themeStyle : themeStyleList) {
      StyleCode styleCode = themeStyle.getColorStyle().getStyleCode();
      List<PlatformColorStyle> platformColorStyles = platformColorStyleMap.get(styleCode);
      if (platformColorStyles == null || platformColorStyles.isEmpty()) {
        throw new RuntimeException(
            "platformColorStyle is null with styleCode :" + styleCode.name());
      }
      for (PlatformColorStyle ps : platformColorStyles) {
        androidColorList.add(AndroidColorDto.fromEntity(themeStyle, ps));
      }
    }
    // 색상 적용
    androidColorStyleEditor.editColors(themeId.toString(), androidColorList);
  }

  /**
   * 2-3. APK 빌드 및 서명을 위한 Docker 실행 명령어를 생성한다.
   *
   * @param sourceThemePath 빌드 대상 테마 디렉토리
   * @return Docker 실행 명령어
   */
  private String[] createDockerCommandForApkBuild(Path sourceThemePath, String themeIdentifier) {
    List<String> command = List.of(
        "docker", "run", "--rm",
        "-v", sourceThemePath.toAbsolutePath() + ":/" + DOCKER_THEME_DIRECTORY_NAME,
        "-v", "gradle-cache:/root/.gradle",
        "-v", signingProperties.getKeystoreHostPath() + ":/secrets",
        "-e", "KEYSTORE_PASSWORD=" + signingProperties.getKeystorePassword(),
        "-e", "KEY_ALIAS=" + signingProperties.getKeyAlias(),
        "-e", "KEY_PASSWORD=" + signingProperties.getKeyPassword(),
        "android:test",
        "assembleRelease",
        "-PandroidApplicationId=" + "com.kakao.talk.theme." + themeIdentifier,
        "-x", "lint",
        "-x", "test"
    );
    return command.toArray(String[]::new);
  }

  /**
   * 2-4. 생성된 APK를 업로드하고 업로드된 파일의 URL을 반환한다.
   *
   * @param themeId 테마 식별자
   * @return 업로드된 APK의 URL
   */
  private String uploadTheme(Integer themeId) {
    Path outputApk = ThemePathManager.getAndroidThemeOutputPath(themeId.toString());
    try {
      String themeUrl = fileManager.uploadFile(Files.readAllBytes(outputApk),
          UUID.randomUUID() + ".apk");
      if (themeUrl == null || themeUrl.isBlank()) {
        throw new RuntimeException("uploaded themeUrl is null");
      }
      return themeUrl;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
