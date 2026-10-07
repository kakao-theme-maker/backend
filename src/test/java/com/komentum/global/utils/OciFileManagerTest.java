package com.komentum.global.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.komentum.global.properties.OciObjectStorageProperty;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails;
import com.oracle.bmc.objectstorage.model.PreauthenticatedRequest;
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;

@ExtendWith(MockitoExtension.class)
class OciFileManagerTest {

  private static final String NAMESPACE = "test-namespace";
  private static final String PUBLIC_IMAGE_BUCKET_NAME = "public-images";
  private static final String PRIVATE_BUCKET_NAME = "private-packages";
  private static final String ENDPOINT = "https://objectstorage.example.com";
  private static final Duration PAR_TTL = Duration.ofHours(48);
  private static final String PUBLIC_FILE_URL_PREFIX = ENDPOINT
      + "/n/" + NAMESPACE
      + "/b/" + PUBLIC_IMAGE_BUCKET_NAME
      + "/o/";

  @Mock
  private ObjectStorage objectStorage;

  private OciFileManager ociFileManager;

  @BeforeEach
  void setUp() {
    OciObjectStorageProperty property = new OciObjectStorageProperty(
        NAMESPACE,
        PUBLIC_IMAGE_BUCKET_NAME,
        PRIVATE_BUCKET_NAME,
        ENDPOINT,
        PAR_TTL);
    ociFileManager = new OciFileManager(objectStorage, property);
  }

  @Test
  @DisplayName("공백, 한글, 경로 구분자가 포함된 공개 객체명을 인코딩하고 복원한다")
  void resolveAndConvertPublicFileUrl_koreanSpaceAndSlash_success() {
    String fileName = "테마 이미지/미리 보기 파일.png";
    String encodedFileName =
        "%ED%85%8C%EB%A7%88%20%EC%9D%B4%EB%AF%B8%EC%A7%80/"
            + "%EB%AF%B8%EB%A6%AC%20%EB%B3%B4%EA%B8%B0%20%ED%8C%8C%EC%9D%BC.png";

    String firstFileUrl = ociFileManager.resolvePublicFileUrl(fileName);
    String secondFileUrl = ociFileManager.resolvePublicFileUrl(fileName);

    assertThat(firstFileUrl).isEqualTo(PUBLIC_FILE_URL_PREFIX + encodedFileName);
    assertThat(secondFileUrl).isEqualTo(firstFileUrl);
    assertThat(ociFileManager.convertUrlToFileName(firstFileUrl)).isEqualTo(fileName);
    verifyNoInteractions(objectStorage);
  }

  @Test
  @DisplayName("OCI 공개 URL 접두사와 일치하지 않는 URL은 객체명으로 변환하지 않는다")
  void convertUrlToFileName_invalidPrefix_throwsException() {
    assertThatThrownBy(
        () -> ociFileManager.convertUrlToFileName("https://example.com/themes/image.png"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("빈 객체명과 null URL을 거부한다")
  void resolveAndConvertPublicFileUrl_nullOrBlank_throwsException() {
    assertThatThrownBy(() -> ociFileManager.resolvePublicFileUrl("  "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ociFileManager.convertUrlToFileName(null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ociFileManager.convertUrlToFileName("  "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ociFileManager.convertUrlToFileName(PUBLIC_FILE_URL_PREFIX))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("공개 이미지 byte 배열을 공개 버킷에 업로드하고 고정 URL을 반환한다")
  void uploadPublicFile_byteArray_success() throws IOException {
    byte[] fileBytes = "file-content".getBytes(StandardCharsets.UTF_8);
    String fileName = "themes/file.png";

    String fileUrl = ociFileManager.uploadPublicFile(fileBytes, fileName);

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    PutObjectRequest request = captor.getValue();
    assertThat(request.getNamespaceName()).isEqualTo(NAMESPACE);
    assertThat(request.getBucketName()).isEqualTo(PUBLIC_IMAGE_BUCKET_NAME);
    assertThat(request.getObjectName()).isEqualTo(fileName);
    assertThat(request.getContentLength()).isEqualTo((long) fileBytes.length);
    assertThat(request.getContentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
    assertThat(request.getPutObjectBody().readAllBytes()).isEqualTo(fileBytes);
    assertThat(fileUrl).isEqualTo(PUBLIC_FILE_URL_PREFIX + fileName);
    verify(objectStorage, never())
        .createPreauthenticatedRequest(any(CreatePreauthenticatedRequestRequest.class));
  }

  @Test
  @DisplayName("JPEG 확장자는 image/jpeg Content-Type으로 업로드한다")
  void uploadPublicFile_jpegContentType_success() {
    ociFileManager.uploadPublicFile(new byte[] {1}, "themes/file.JPEG");

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    assertThat(captor.getValue().getContentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
  }

  @Test
  @DisplayName("공개 InputStream 업로드에 기본 MIME type을 적용하고 stream을 닫는다")
  void uploadPublicFile_inputStream_success() throws IOException {
    byte[] fileBytes = "stream-content".getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = spy(new ByteArrayInputStream(fileBytes));
    String fileName = "themes/theme.html";

    ociFileManager.uploadPublicFile(inputStream, fileBytes.length, fileName);

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    PutObjectRequest request = captor.getValue();
    assertThat(request.getBucketName()).isEqualTo(PUBLIC_IMAGE_BUCKET_NAME);
    assertThat(request.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    assertThat(request.getPutObjectBody()).isSameAs(inputStream);
    verify(inputStream).close();
  }

  @Test
  @DisplayName("패키지 byte 배열은 private 버킷에 업로드하고 파일명만 반환한다")
  void uploadThemePackageAndGetFileName_byteArray_usesPrivateBucket() throws IOException {
    byte[] fileBytes = "private-content".getBytes(StandardCharsets.UTF_8);
    String fileName = "themes/private.APK";

    String uploadedFileName = ociFileManager.uploadThemePackageAndGetFileName(
        fileBytes, fileName, "apk");

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    PutObjectRequest request = captor.getValue();
    assertThat(request.getNamespaceName()).isEqualTo(NAMESPACE);
    assertThat(request.getBucketName()).isEqualTo(PRIVATE_BUCKET_NAME);
    assertThat(request.getObjectName()).isEqualTo(fileName);
    assertThat(request.getContentLength()).isEqualTo((long) fileBytes.length);
    assertThat(request.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    assertThat(request.getPutObjectBody().readAllBytes()).isEqualTo(fileBytes);
    assertThat(uploadedFileName).isEqualTo(fileName);
    verify(objectStorage, never())
        .createPreauthenticatedRequest(any(CreatePreauthenticatedRequestRequest.class));
  }

  @Test
  @DisplayName("패키지 스트림은 private 버킷에 UUID 파일명으로 업로드하고 스트림을 닫는다")
  void uploadThemePackageAndGetFileName_inputStream_generatesNameAndClosesStream() throws IOException {
    byte[] fileBytes = "stream-content".getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = spy(new ByteArrayInputStream(fileBytes));

    String uploadedFileName = ociFileManager.uploadThemePackageAndGetFileName(
        inputStream, fileBytes.length, " ", ".apk");

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    PutObjectRequest request = captor.getValue();
    assertThat(request.getBucketName()).isEqualTo(PRIVATE_BUCKET_NAME);
    assertThat(request.getObjectName()).isEqualTo(uploadedFileName);
    assertThat(uploadedFileName).endsWith(".apk");
    assertThat(UUID.fromString(uploadedFileName.substring(0, uploadedFileName.length() - 4)))
        .isNotNull();
    assertThat(request.getPutObjectBody()).isSameAs(inputStream);
    verify(inputStream).close();
    verify(objectStorage, never())
        .createPreauthenticatedRequest(any(CreatePreauthenticatedRequestRequest.class));
  }

  @Test
  @DisplayName("private 객체용 ObjectRead PAR을 설정된 48시간 TTL로 생성한다")
  void createThemePackageDownloadUrl_success() {
    String fileName = "themes/private.ktheme";
    String accessUri = "/p/token/n/test-namespace/b/private-packages/o/themes/private.ktheme";
    given(objectStorage.createPreauthenticatedRequest(
        any(CreatePreauthenticatedRequestRequest.class)))
        .willReturn(CreatePreauthenticatedRequestResponse.builder()
            .preauthenticatedRequest(PreauthenticatedRequest.builder()
                .accessUri(accessUri)
                .build())
            .build());
    Instant beforeCreation = Instant.now();

    String downloadUrl = ociFileManager.createThemePackageDownloadUrl(fileName);

    Instant afterCreation = Instant.now();
    ArgumentCaptor<CreatePreauthenticatedRequestRequest> captor =
        ArgumentCaptor.forClass(CreatePreauthenticatedRequestRequest.class);
    verify(objectStorage).createPreauthenticatedRequest(captor.capture());
    CreatePreauthenticatedRequestRequest request = captor.getValue();
    CreatePreauthenticatedRequestDetails details =
        request.getCreatePreauthenticatedRequestDetails();
    assertThat(request.getNamespaceName()).isEqualTo(NAMESPACE);
    assertThat(request.getBucketName()).isEqualTo(PRIVATE_BUCKET_NAME);
    assertThat(details.getObjectName()).isEqualTo(fileName);
    assertThat(details.getAccessType())
        .isEqualTo(CreatePreauthenticatedRequestDetails.AccessType.ObjectRead);
    assertThat(details.getTimeExpires().toInstant()).isBetween(
        beforeCreation.plus(PAR_TTL).minusSeconds(1),
        afterCreation.plus(PAR_TTL).plusSeconds(1));
    assertThat(downloadUrl).isEqualTo(ENDPOINT + accessUri);
  }

  @Test
  @DisplayName("OCI가 유효한 PAR을 반환하지 않으면 다운로드 URL 생성을 실패한다")
  void createThemePackageDownloadUrl_emptyResponse_throwsException() {
    given(objectStorage.createPreauthenticatedRequest(
        any(CreatePreauthenticatedRequestRequest.class))).willReturn(null);

    assertThatThrownBy(() -> ociFileManager.createThemePackageDownloadUrl("themes/private.apk"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  @DisplayName("공개 객체를 byte 배열로 다운로드하고 응답 stream을 닫는다")
  void downloadFile_success() throws IOException {
    byte[] fileBytes = "download-content".getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = spy(new ByteArrayInputStream(fileBytes));
    given(objectStorage.getObject(any(GetObjectRequest.class)))
        .willReturn(GetObjectResponse.builder().inputStream(inputStream).build());

    byte[] result = ociFileManager.downloadFile("themes/download.txt");

    ArgumentCaptor<GetObjectRequest> captor = ArgumentCaptor.forClass(GetObjectRequest.class);
    verify(objectStorage).getObject(captor.capture());
    GetObjectRequest request = captor.getValue();
    assertThat(request.getNamespaceName()).isEqualTo(NAMESPACE);
    assertThat(request.getBucketName()).isEqualTo(PUBLIC_IMAGE_BUCKET_NAME);
    assertThat(request.getObjectName()).isEqualTo("themes/download.txt");
    assertThat(result).isEqualTo(fileBytes);
    verify(inputStream).close();
  }

  @Test
  @DisplayName("공개 객체 삭제 요청에 namespace, public bucket과 객체명을 전달한다")
  void deleteFile_success() {
    String fileName = "themes/delete.png";

    ociFileManager.deleteFile(fileName);

    ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(objectStorage).deleteObject(captor.capture());
    DeleteObjectRequest request = captor.getValue();
    assertThat(request.getNamespaceName()).isEqualTo(NAMESPACE);
    assertThat(request.getBucketName()).isEqualTo(PUBLIC_IMAGE_BUCKET_NAME);
    assertThat(request.getObjectName()).isEqualTo(fileName);
  }

  @Test
  @DisplayName("byte 배열 다운로드 중 IOException이 발생하면 UncheckedIOException으로 변환한다")
  void downloadFile_ioException_throwsUncheckedIOException() {
    InputStream inputStream = new InputStream() {
      @Override
      public int read() throws IOException {
        throw new IOException("forced read failure");
      }
    };
    given(objectStorage.getObject(any(GetObjectRequest.class)))
        .willReturn(GetObjectResponse.builder().inputStream(inputStream).build());

    assertThatThrownBy(() -> ociFileManager.downloadFile("themes/fail.bin"))
        .isInstanceOf(UncheckedIOException.class)
        .hasCauseInstanceOf(IOException.class);
  }
}
