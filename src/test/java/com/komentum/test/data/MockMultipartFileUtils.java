package com.komentum.test.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.Getter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

public class MockMultipartFileUtils {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Getter
  public enum ImageExtension {
    JPEG("jpeg", MediaType.IMAGE_JPEG),
    PNG("png", MediaType.IMAGE_PNG);

    private final String extension;
    private final MediaType mediaType;

    ImageExtension(String extension, MediaType mediaType) {
      this.extension = extension;
      this.mediaType = mediaType;
    }
  }

  public static MockMultipartFile generateImageFormData(String fileName, ImageExtension extension) {
    String originalFileName = extension.getExtension().startsWith(".") ?
        fileName + extension.getExtension() :
        fileName + "." + extension.getExtension();
    return new MockMultipartFile(
        fileName,
        originalFileName,
        extension.getMediaType().toString(),
        originalFileName.getBytes()
    );
  }

  public static <T> MockMultipartFile generateJsonFormData(String fileName, T jsonData) {
    try {
      return new MockMultipartFile(
          fileName,
          null,
          MediaType.APPLICATION_JSON.toString(),
          objectMapper.writeValueAsBytes(jsonData)
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException("failed to convert json to byte : " + fileName, e);
    }
  }

  /**
   * 테스트 리소스(sample-images)의 실제 PNG 파일 내용으로 part 이름을 지정한 이미지 form data를 생성한다.
   */
  public static MockMultipartFile generateSamplePngFormData(String partName) {
    try {
      byte[] content = new ClassPathResource("sample-images/test.png").getContentAsByteArray();
      return generateImageFormData(partName, content);
    } catch (IOException e) {
      throw new RuntimeException("failed to read sample image : test.png", e);
    }
  }

  /**
   * 파일 내용을 직접 지정한 PNG 이미지 form data를 생성한다. Content-Type, 파일명은 PNG이므로 실제 포맷 검증 테스트에 사용할 수 있다.
   */
  public static MockMultipartFile generateImageFormData(String partName, byte[] content) {
    return new MockMultipartFile(
        partName,
        "image.png",
        MediaType.IMAGE_PNG.toString(),
        content
    );
  }

  /**
   * 내용이 비어있는 이미지 form data를 생성한다.
   */
  public static MockMultipartFile generateEmptyImageFormData(String partName) {
    return generateImageFormData(partName, new byte[0]);
  }

  /**
   * JSON을 Blob으로 전송한 것처럼 filename="blob"인 JSON form data를 생성한다. 이 part는 서버에서 파일로도 분류된다.
   */
  public static <T> MockMultipartFile generateJsonBlobFormData(String partName, T jsonData) {
    try {
      return new MockMultipartFile(
          partName,
          "blob",
          MediaType.APPLICATION_JSON.toString(),
          objectMapper.writeValueAsBytes(jsonData)
      );
    } catch (JsonProcessingException e) {
      throw new RuntimeException("failed to convert json to byte : " + partName, e);
    }
  }

  /**
   * 가로, 세로 크기를 지정한 실제 PNG 이미지 form data를 생성한다.
   */
  public static MockMultipartFile generatePngFormData(String partName, int width, int height) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), "png", baos);
      return generateImageFormData(partName, baos.toByteArray());
    } catch (IOException e) {
      throw new RuntimeException("failed to generate png image : " + partName, e);
    }
  }
}
