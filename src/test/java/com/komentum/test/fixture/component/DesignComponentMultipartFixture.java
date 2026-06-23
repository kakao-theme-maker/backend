package com.komentum.test.fixture.component;

import com.komentum.test.MockMvcUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DesignComponentMultipartFixture {

  private static final byte[] DEFAULT_IMAGE_CONTENT = "test-image-content".getBytes();

  private final MockMvcUtils mockMvcUtils;

  public MockMultipartFile imagePart(String fileName) {
    return mockMvcUtils.fileToTestFormData(
        "image",
        fileName,
        MediaType.IMAGE_PNG,
        DEFAULT_IMAGE_CONTENT
    );
  }

  public MockMultipartFile filesPart(String fileName) {
    return mockMvcUtils.fileToTestFormData(
        "files",
        fileName,
        MediaType.IMAGE_PNG,
        DEFAULT_IMAGE_CONTENT
    );
  }

  public MockMultipartFile emptyFilesPart(String fileName) {
    return mockMvcUtils.fileToTestFormData(
        "files",
        fileName,
        MediaType.IMAGE_PNG,
        new byte[0]
    );
  }
}
