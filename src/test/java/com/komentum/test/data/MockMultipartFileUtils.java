package com.komentum.test.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
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
}
