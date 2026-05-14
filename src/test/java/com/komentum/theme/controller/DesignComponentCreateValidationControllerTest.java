package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("DesignComponent 생성 검증 실패 테스트")
class DesignComponentCreateValidationControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("DesignComponent 생성 시 componentTypeIds 누락 검증")
  void createDesignComponent_withoutComponentTypeIds() throws Exception {
    CreateDesignComponentRequest createRequest = createRequestWithoutComponentTypeIds(true);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile image = createImagePart("test.png");

    doMultipartRequest(
        "/api/design-components",
        HttpMethod.POST,
        400,
        new TypeReference<Void>() {
        },
        requestPart,
        image
    );

    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }

  @Test
  @DisplayName("다중 업로드 생성 시 존재하지 않는 componentTypeId 검증")
  void createDesignComponents_withUnknownComponentTypeId() throws Exception {
    CreateDesignComponentRequest createRequest = createRequest(true, List.of(999999));
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile file = createFilesPart("test.png");

    doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        404,
        new TypeReference<Void>() {
        },
        requestPart,
        file
    );

    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }

  @Test
  @DisplayName("다중 업로드 생성 시 파일 누락 검증")
  void createDesignComponent_withoutImageAndFiles() throws Exception {
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA);
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
  void createDesignComponent_withEmptyFile() throws Exception {
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile emptyFile = emptyFilesPart("empty.png");

    doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        400,
        new TypeReference<Void>() {
        },
        requestPart,
        emptyFile
    );

    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }

  @Test
  @DisplayName("다중 업로드 생성 시 일부 빈 파일이 있으면 업로드를 시작하지 않는다")
  void createDesignComponents_withEmptyFileAfterValidFile() throws Exception {
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile validFile = createFilesPart("valid.png");
    MockMultipartFile emptyFile = emptyFilesPart("empty.png");

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
    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }

  @Test
  @DisplayName("다중 업로드 생성 시 componentTypeIds 단일값 강제 검증")
  void createDesignComponents_requiresSingleComponentTypeId() throws Exception {
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA,
        componentTypeB);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile file = createFilesPart("single-file.png");

    doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        400,
        new TypeReference<Void>() {
        },
        requestPart,
        file
    );

    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }

  @Test
  @DisplayName("DesignComponent 생성 시 중복 componentTypeIds 검증")
  void createDesignComponent_duplicateComponentTypeIds() throws Exception {
    CreateDesignComponentRequest createRequest = createRequest(true,
        List.of(componentTypeA.getComponentTypeId(), componentTypeA.getComponentTypeId()));
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile image = createImagePart("test.png");

    doMultipartRequest(
        "/api/design-components",
        HttpMethod.POST,
        400,
        new TypeReference<Void>() {
        },
        requestPart,
        image
    );
  }

  @Test
  @DisplayName("DesignComponent 생성 시 존재하지 않는 componentTypeId 검증")
  void createDesignComponent_withUnknownComponentTypeId() throws Exception {
    CreateDesignComponentRequest createRequest = createRequest(true,
        List.of(componentTypeA.getComponentTypeId(), 999999));
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile image = createImagePart("test.png");

    doMultipartRequest(
        "/api/design-components",
        HttpMethod.POST,
        404,
        new TypeReference<Void>() {
        },
        requestPart,
        image
    );

    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }
}
