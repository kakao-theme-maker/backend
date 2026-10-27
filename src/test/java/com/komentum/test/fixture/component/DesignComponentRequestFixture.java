package com.komentum.test.fixture.component;

import com.komentum.test.data.MockMultipartFileUtils;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import com.komentum.designcomponent.dto.UpdateDesignComponentRequest;
import org.springframework.mock.web.MockMultipartFile;

public final class DesignComponentRequestFixture {

  public static final String IMAGE_BASE_URL = "https://cdn.example.com/";

  private DesignComponentRequestFixture() {
  }

  /**
   * FileManager.resolvePublicFileUrl 목 응답과 동일한 규칙으로 파일명에 대한 이미지 URL을 생성한다.
   */
  public static String imageUrlOf(String fileName) {
    return IMAGE_BASE_URL + fileName;
  }

  /**
   * 타입 정보 없이 공개 디자인 에셋 생성 요청을 만든다.
   */
  public static CreateDesignComponentRequest publicCreateRequest() {
    return createRequest(true);
  }

  /**
   * 공개 여부를 지정한 디자인 에셋 생성 요청을 만든다.
   */
  public static CreateDesignComponentRequest createRequest(boolean isPublic) {
    return CreateDesignComponentRequest.builder()
        .isPublic(isPublic)
        .build();
  }

  /**
   * 공개 여부를 지정한 디자인 에셋 수정 요청을 만든다.
   */
  public static UpdateDesignComponentRequest updateRequest(boolean isPublic) {
    return UpdateDesignComponentRequest.builder()
        .isPublic(isPublic)
        .build();
  }

  /**
   * 요청 객체를 디자인 에셋 API의 JSON multipart 파트로 변환한다.
   */
  public static MockMultipartFile createRequestPart(Object request) {
    return MockMultipartFileUtils.generateJsonFormData("request", request);
  }
}
