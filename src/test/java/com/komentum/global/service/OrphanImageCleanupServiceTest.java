package com.komentum.global.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.komentum.global.utils.FileManager;
import com.komentum.global.utils.FileUtils;
import com.komentum.post.domain.Post;
import com.komentum.post.repository.PostRepository;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.repository.DesignComponentRepository;
import com.komentum.user.repository.UserRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrphanImageCleanupServiceTest {

  private static final String TEST_ENTITY = "com.komentum.test.Entity";
  private static final String TEST_CDN_URL = "https://cdn.example.com/";

  @Mock
  FileManager fileManager;
  @Mock
  FileUtils fileUtils;
  @Mock
  DesignComponentRepository designComponentRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  PostRepository postRepository;

  @InjectMocks
  OrphanImageCleanupService service;
  

  private String generateFileName(long timestamp) {
    return generateFileName(timestamp, "jpg");
  }

  private String generateFileName(long timestamp, String extension) {
    return TEST_ENTITY + "_" + UUID.randomUUID() + "_" + timestamp + "." + extension;
  }

  private long daysAgo(int days) {
    return System.currentTimeMillis() - Duration.ofDays(days).toMillis();
  }

  private void givenEmptyRepositories() {
    given(designComponentRepository.findAll()).willReturn(List.of());
    given(userRepository.findAll()).willReturn(List.of());
    given(postRepository.findAll()).willReturn(List.of());
  }

  // === Tests ===

  @Nested
  @DisplayName("extractTimestamp")
  class ExtractTimestampTest {

    @Test
    @DisplayName("유효한 파일명에서 timestamp 추출")
    void validFileName_returnsTimestamp() {
      // given
      String fileName = TEST_ENTITY + "_550e8400-e29b-41d4-a716-446655440000_1704067200000.jpg";
      // when
      Long result = service.extractTimestamp(fileName);
      // then
      assertThat(result).isEqualTo(1704067200000L);
    }

    @Test
    @DisplayName("다른 확장자도 추출 성공")
    void differentExtension_returnsTimestamp() {
      // given
      String fileName = TEST_ENTITY + "_a1b2c3d4-e5f6-7890-abcd-ef1234567890_1700000000000.png";
      // when
      Long result = service.extractTimestamp(fileName);
      // then
      assertThat(result).isEqualTo(1700000000000L);
    }

    @Test
    @DisplayName("잘못된 형식은 null 반환")
    void invalidFormat_returnsNull() {
      // given
      String fileName = "invalid-file-name.jpg";
      // when & then
      assertThat(service.extractTimestamp(fileName)).isNull();
    }

    @Test
    @DisplayName("timestamp 없으면 null 반환")
    void noTimestamp_returnsNull() {
      // given
      String fileName = TEST_ENTITY + "_550e8400-e29b-41d4-a716-446655440000.jpg";
      // when & then
      assertThat(service.extractTimestamp(fileName)).isNull();
    }

    @Test
    @DisplayName("null 입력은 null 반환")
    void nullInput_returnsNull() {
      // when & then
      assertThat(service.extractTimestamp(null)).isNull();
    }
  }

  @Nested
  @DisplayName("cleanupOrphanImages")
  class CleanupOrphanImagesTest {

    @Test
    @DisplayName("7일 이상 된 고아 파일 삭제")
    void deletesOldOrphanFiles() {
      // given
      String orphanFile = generateFileName(daysAgo(8));
      given(fileManager.listAllFileNames()).willReturn(List.of(orphanFile));
      givenEmptyRepositories();
      // when
      int deletedCount = service.cleanupOrphanImages();
      // then
      assertThat(deletedCount).isEqualTo(1);
      verify(fileUtils).deleteFileSilently(eq(orphanFile), anyString());
    }

    @Test
    @DisplayName("7일 미만 고아 파일은 유지")
    void skipsRecentOrphanFiles() {
      // given
      String recentFile = generateFileName(daysAgo(3));
      given(fileManager.listAllFileNames()).willReturn(List.of(recentFile));
      givenEmptyRepositories();
      // when
      int deletedCount = service.cleanupOrphanImages();
      // then
      assertThat(deletedCount).isEqualTo(0);
      verify(fileUtils, never()).deleteFileSilently(anyString(), anyString());
    }

    @Test
    @DisplayName("DesignComponent에서 참조된 파일은 유지")
    void skipsDesignComponentReferencedFiles() {
      // given
      String referencedFile = generateFileName(daysAgo(8));
      String fileUrl = TEST_CDN_URL + referencedFile;
      DesignComponent designComponent = DesignComponent.builder()
          .imageUrl(fileUrl)
          .build();
      given(fileManager.listAllFileNames()).willReturn(List.of(referencedFile));
      given(fileManager.convertUrlToFileName(fileUrl)).willReturn(referencedFile);
      given(designComponentRepository.findAll()).willReturn(List.of(designComponent));
      given(userRepository.findAll()).willReturn(List.of());
      given(postRepository.findAll()).willReturn(List.of());
      // when
      int deletedCount = service.cleanupOrphanImages();
      // then
      assertThat(deletedCount).isEqualTo(0);
      verify(fileUtils, never()).deleteFileSilently(anyString(), anyString());
    }

    @Test
    @DisplayName("Post에서 참조된 파일은 유지")
    void skipsPostReferencedFiles() {
      // given
      String referencedFile = generateFileName(daysAgo(8));
      Post post = Post.builder()
          .previewImageName(referencedFile)
          .build();
      given(fileManager.listAllFileNames()).willReturn(List.of(referencedFile));
      given(designComponentRepository.findAll()).willReturn(List.of());
      given(userRepository.findAll()).willReturn(List.of());
      given(postRepository.findAll()).willReturn(List.of(post));
      // when
      int deletedCount = service.cleanupOrphanImages();
      // then
      assertThat(deletedCount).isEqualTo(0);
      verify(fileUtils, never()).deleteFileSilently(anyString(), anyString());
    }

    @Test
    @DisplayName("여러 파일 중 고아 파일만 삭제")
    void deletesOnlyOrphanFiles() {
      // given
      String orphanFile = generateFileName(daysAgo(8));
      String referencedFile = generateFileName(daysAgo(8), "png");
      String fileUrl = TEST_CDN_URL + referencedFile;
      DesignComponent designComponent = DesignComponent.builder()
          .imageUrl(fileUrl)
          .build();
      given(fileManager.listAllFileNames()).willReturn(List.of(orphanFile, referencedFile));
      given(fileManager.convertUrlToFileName(fileUrl)).willReturn(referencedFile);
      given(designComponentRepository.findAll()).willReturn(List.of(designComponent));
      given(userRepository.findAll()).willReturn(List.of());
      given(postRepository.findAll()).willReturn(List.of());
      // when
      int deletedCount = service.cleanupOrphanImages();
      // then
      assertThat(deletedCount).isEqualTo(1);
      verify(fileUtils).deleteFileSilently(eq(orphanFile), anyString());
    }
  }
}
