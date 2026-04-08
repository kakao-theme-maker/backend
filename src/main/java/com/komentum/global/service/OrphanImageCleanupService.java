package com.komentum.global.service;

import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.FileUtils;
import com.komentum.post.repository.PostRepository;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.repository.UserRepository;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrphanImageCleanupService {

  private final FileManager fileManager;
  private final FileUtils fileUtils;
  private final DesignComponentRepository designComponentRepository;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  private static final long ORPHAN_THRESHOLD_DAYS = 7;
  private static final long ORPHAN_THRESHOLD_MILLIS = Duration.ofDays(ORPHAN_THRESHOLD_DAYS)
      .toMillis();

  // 파일명에서 timestamp 추출 정규식
  // 형식: {entityName}_{UUID}_{timestamp}.{extension}
  private static final Pattern TIMESTAMP_PATTERN =
      Pattern.compile("^.+_[a-f0-9\\-]{36}_(\\d{13})\\.\\w+$");

  /**
   * OrphanImage 삭제
   *
   * @return int 삭제된 데이터 수
   */
  @Transactional(readOnly = true)
  public int cleanupOrphanImages() {
    log.info("Starting orphan image cleanup");

    // S3에 있는 모든 파일 스캔 (규모 커지면 개선 필요해보임)
    List<String> allStoredFileNames = fileManager.listAllFileNames();
    log.info("Total files in storage: {}", allStoredFileNames.size());

    // DB에서 사용중인 파일 스캔
    Set<String> usedFileNames = collectUsedFileNames();
    log.info("Total files referenced in DB: {}", usedFileNames.size());

    long currentTimeMillis = System.currentTimeMillis();
    int deletedCount = 0;

    for (String fileName : allStoredFileNames) {
      if (usedFileNames.contains(fileName)) {
        continue;
      }

      Long fileTimestamp = extractTimestamp(fileName);
      if (fileTimestamp == null) {
        log.warn("Could not extract timestamp from file: {}", fileName);
        continue;
      }

      // 업로드 후 아직 DB 반영 안될 수 있기에 7일 지난 파일만 삭제
      long fileAge = currentTimeMillis - fileTimestamp;
      if (fileAge >= ORPHAN_THRESHOLD_MILLIS) {
        fileUtils.deleteFileSilently(fileName,
            "Failed to delete orphan file: " + fileName);
        deletedCount++;
        log.debug("Deleted orphan file: {} (age: {} days)",
            fileName, Duration.ofMillis(fileAge).toDays());
      }
    }

    log.info("Orphan image cleanup completed. Deleted {} files.", deletedCount);
    return deletedCount;
  }

  /**
   * DB에 참조되고 있는 파일 수집
   *
   * @return set usedFileNames
   */
  private Set<String> collectUsedFileNames() {
    Set<String> usedFileNames = new HashSet<>();

    // DesignComponent.imageUrl -> fileName 변환
    designComponentRepository.findAll().stream()
        .map(dc -> dc.getImageUrl())
        .filter(url -> url != null && !url.isEmpty())
        .map(this::safeConvertUrlToFileName)
        .filter(name -> name != null)
        .forEach(usedFileNames::add);

    // User.profileImg -> fileName 변환
    userRepository.findAll().stream()
        .map(user -> user.getProfileImg())
        .filter(url -> url != null && !url.isEmpty())
        .map(this::safeConvertUrlToFileName)
        .filter(name -> name != null)
        .forEach(usedFileNames::add);

    // Post.previewImageName (이미 fileName 형태)
    postRepository.findAll().stream()
        .map(post -> post.getPreviewImageName())
        .filter(name -> name != null && !name.isEmpty())
        .forEach(usedFileNames::add);

    return usedFileNames;
  }

  // 실패 시 예외처리
  private String safeConvertUrlToFileName(String url) {
    try {
      return fileManager.convertUrlToFileName(url);
    } catch (Exception e) {
      log.warn("Failed to convert URL to fileName: {}", url);
      return null;
    }
  }

  // 파일명에서 생성 시간 추출
  Long extractTimestamp(String fileName) {
    if (fileName == null || fileName.isEmpty()) {
      return null;
    }

    Matcher matcher = TIMESTAMP_PATTERN.matcher(fileName);
    if (matcher.matches()) {
      try {
        return Long.parseLong(matcher.group(1));
      } catch (NumberFormatException e) {
        return null;
      }
    }
    return null;
  }
}
