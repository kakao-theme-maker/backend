package com.komentum.test.fixture.component;

import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.designcomponent.domain.ComponentType;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.UpdateDesignComponentRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.mock.web.MockMultipartFile;

public final class DesignComponentRequestFixture {

  public static final String IMAGE_BASE_URL = "https://cdn.example.com/";

  private DesignComponentRequestFixture() {
  }

  /**
   * FileManager.resolveFilePath 목 응답과 동일한 규칙으로 파일명에 대한 이미지 URL을 생성한다.
   */
  public static String imageUrlOf(String fileName) {
    return IMAGE_BASE_URL + fileName;
  }

  public static CreateDesignComponentRequest publicCreateRequest(ComponentType... componentTypes) {
    return createRequest(true, componentTypes);
  }

  public static CreateDesignComponentRequest createRequest(boolean isPublic,
      ComponentType... componentTypes) {
    return createRequest(isPublic, componentTypeIds(componentTypes));
  }

  public static CreateDesignComponentRequest createRequest(boolean isPublic,
      List<Integer> componentTypeIds) {
    return CreateDesignComponentRequest.builder()
        .isPublic(isPublic)
        .componentTypeIds(componentTypeIds)
        .build();
  }

  public static CreateDesignComponentRequest createRequestWithoutComponentTypeIds(
      boolean isPublic) {
    return CreateDesignComponentRequest.builder()
        .isPublic(isPublic)
        .build();
  }

  public static UpdateDesignComponentRequest updateRequest(boolean isPublic,
      ComponentType... componentTypes) {
    return updateRequest(isPublic, componentTypeIds(componentTypes));
  }

  public static UpdateDesignComponentRequest updateRequest(boolean isPublic,
      List<Integer> componentTypeIds) {
    return UpdateDesignComponentRequest.builder()
        .isPublic(isPublic)
        .componentTypeIds(componentTypeIds)
        .build();
  }

  public static MockMultipartFile createRequestPart(Object request) {
    return MockMultipartFileUtils.generateJsonFormData("request", request);
  }

  public static List<Integer> componentTypeIds(ComponentType... componentTypes) {
    return Arrays.stream(componentTypes)
        .map(ComponentType::getComponentTypeId)
        .toList();
  }
}
