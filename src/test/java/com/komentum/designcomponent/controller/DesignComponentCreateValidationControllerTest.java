package com.komentum.designcomponent.controller;

import static com.komentum.test.fixture.component.DesignComponentRequestFixture.createRequestPart;
import static com.komentum.test.fixture.component.DesignComponentRequestFixture.publicCreateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.dto.CreateDesignComponentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("DesignComponent 생성 검증 실패 테스트")
class DesignComponentCreateValidationControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("다중 업로드 생성 시 파일 누락 검증")
  void createDesignComponents_withoutFiles() throws Exception {
    CreateDesignComponentRequest createRequest = publicCreateRequest();
    MockMultipartFile requestPart = createRequestPart(createRequest);

    doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        400,
        new TypeReference<Void>() {
        },
        requestPart
    );
  }

  @Test
  @DisplayName("다중 업로드 생성 시 빈 파일 검증")
  void createDesignComponents_withEmptyFile() throws Exception {
    CreateDesignComponentRequest createRequest = publicCreateRequest();
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile emptyFile = multipartFixture.emptyFilesPart("empty.png");

    doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        400,
        new TypeReference<Void>() {
        },
        requestPart,
        emptyFile
    );

    verify(fileManager, never()).uploadPublicFile(any(byte[].class), anyString());
  }

  @Test
  @DisplayName("다중 업로드 생성 시 일부 빈 파일이 있으면 업로드를 시작하지 않는다")
  void createDesignComponents_withEmptyFileAfterValidFile() throws Exception {
    CreateDesignComponentRequest createRequest = publicCreateRequest();
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile validFile = multipartFixture.filesPart("valid.png");
    MockMultipartFile emptyFile = multipartFixture.emptyFilesPart("empty.png");

    doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        400,
        new TypeReference<Void>() {
        },
        requestPart,
        validFile,
        emptyFile
    );

    assertThat(designComponentRepository.count()).isZero();
    verify(fileManager, never()).uploadPublicFile(any(byte[].class), anyString());
  }

}
