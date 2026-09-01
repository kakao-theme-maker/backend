package com.komentum.global.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.komentum.global.properties.FileStorageProperty;
import com.komentum.global.properties.FileStorageProperty.Storage;
import com.komentum.global.properties.OciObjectStorageProperty;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
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
  private static final String BUCKET_NAME = "MyObjectStorage";
  private static final String ENDPOINT = "https://private.objectstorage.example.com";
  private static final String FILE_BASE_URL = "https://theme-maker-test.kro.kr";
  private static final String FILE_URL_PREFIX = FILE_BASE_URL + "/data/uploads/";

  @Mock
  private ObjectStorage objectStorage;

  private OciFileManager ociFileManager;

  @BeforeEach
  void setUp() {
    OciObjectStorageProperty property = new OciObjectStorageProperty(
        NAMESPACE,
        BUCKET_NAME,
        ENDPOINT
    );
    FileStorageProperty fileStorageProperty = new FileStorageProperty(FILE_BASE_URL, Storage.OCI);
    ociFileManager = new OciFileManager(objectStorage, property, fileStorageProperty);
  }

  @Test
  @DisplayName("한글과 공백이 포함된 객체명을 경로 구분자를 유지해 인코딩하고 복원한다")
  void resolveAndConvertFilePath_koreanAndSpace_success() {
    String fileName = "테마 이미지/미리 보기 파일.png";
    String encodedFileName =
        "%ED%85%8C%EB%A7%88%20%EC%9D%B4%EB%AF%B8%EC%A7%80/"
            + "%EB%AF%B8%EB%A6%AC%20%EB%B3%B4%EA%B8%B0%20%ED%8C%8C%EC%9D%BC.png";

    String fileUrl = ociFileManager.resolveFilePath(fileName);

    assertThat(fileUrl).isEqualTo(FILE_URL_PREFIX + encodedFileName);
    assertThat(ociFileManager.convertUrlToFileName(fileUrl)).isEqualTo(fileName);
  }

  @Test
  @DisplayName("file base URL과 일치하지 않는 URL은 객체명으로 변환하지 않는다")
  void convertUrlToFileName_invalidPrefix_throwsException() {
    assertThatThrownBy(
        () -> ociFileManager.convertUrlToFileName("https://example.com/themes/image.png"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("빈 객체명과 null URL을 거부한다")
  void resolveAndConvertFilePath_nullOrBlank_throwsException() {
    assertThatThrownBy(() -> ociFileManager.resolveFilePath("  "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ociFileManager.convertUrlToFileName(null))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ociFileManager.convertUrlToFileName("  "))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> ociFileManager.convertUrlToFileName(FILE_URL_PREFIX))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("byte 배열 업로드 요청에 객체 정보, 길이, MIME type과 본문을 전달한다")
  void uploadFile_byteArray_success() throws IOException {
    byte[] fileBytes = "file-content".getBytes(StandardCharsets.UTF_8);
    String fileName = "themes/file.png";

    String fileUrl = ociFileManager.uploadFile(fileBytes, fileName);

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    PutObjectRequest request = captor.getValue();
    assertThat(request.getNamespaceName()).isEqualTo(NAMESPACE);
    assertThat(request.getBucketName()).isEqualTo(BUCKET_NAME);
    assertThat(request.getObjectName()).isEqualTo(fileName);
    assertThat(request.getContentLength()).isEqualTo((long) fileBytes.length);
    assertThat(request.getContentType()).isEqualTo(MediaType.IMAGE_PNG_VALUE);
    assertThat(request.getPutObjectBody().readAllBytes()).isEqualTo(fileBytes);
    assertThat(fileUrl).isEqualTo(FILE_URL_PREFIX + fileName);
  }

  @Test
  @DisplayName("JPEG 확장자는 image/jpeg Content-Type으로 업로드한다")
  void uploadFile_jpegContentType_success() {
    ociFileManager.uploadFile(new byte[] {1}, "themes/file.JPEG");

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    assertThat(captor.getValue().getContentType()).isEqualTo(MediaType.IMAGE_JPEG_VALUE);
  }

  @Test
  @DisplayName("InputStream 업로드에 기본 MIME type을 적용하고 stream을 닫는다")
  void uploadFile_inputStream_success() throws IOException {
    byte[] fileBytes = "stream-content".getBytes(StandardCharsets.UTF_8);
    InputStream inputStream = spy(new ByteArrayInputStream(fileBytes));
    String fileName = "themes/theme.html";

    ociFileManager.uploadFile(inputStream, fileBytes.length, fileName);

    ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
    verify(objectStorage).putObject(captor.capture());
    PutObjectRequest request = captor.getValue();
    assertThat(request.getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM_VALUE);
    assertThat(request.getPutObjectBody()).isSameAs(inputStream);
    verify(inputStream).close();
  }

  @Test
  @DisplayName("객체를 byte 배열로 다운로드하고 응답 stream을 닫는다")
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
    assertThat(request.getBucketName()).isEqualTo(BUCKET_NAME);
    assertThat(request.getObjectName()).isEqualTo("themes/download.txt");
    assertThat(result).isEqualTo(fileBytes);
    verify(inputStream).close();
  }

  @Test
  @DisplayName("삭제 요청에 namespace, bucket과 객체명을 전달한다")
  void deleteFile_success() {
    String fileName = "themes/delete.png";

    ociFileManager.deleteFile(fileName);

    ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
    verify(objectStorage).deleteObject(captor.capture());
    DeleteObjectRequest request = captor.getValue();
    assertThat(request.getNamespaceName()).isEqualTo(NAMESPACE);
    assertThat(request.getBucketName()).isEqualTo(BUCKET_NAME);
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
