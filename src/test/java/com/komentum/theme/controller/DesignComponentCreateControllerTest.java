package com.komentum.theme.controller;

import static com.komentum.test.fixture.component.DesignComponentRequestFixture.createRequestPart;
import static com.komentum.test.fixture.component.DesignComponentRequestFixture.publicCreateRequest;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.CreateDesignComponentRequest;
import com.komentum.theme.component.dto.DesignComponentDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("DesignComponent 생성 테스트")
class DesignComponentCreateControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("DesignComponent 생성 테스트")
  void createDesignComponent() throws Exception {
    stubImageUpload();
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA, componentTypeB);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile image = multipartFixture.imagePart("test.png");

    DesignComponentDto response = doMultipartRequest(
        "/api/design-components",
        HttpMethod.POST,
        200,
        new TypeReference<>() {
        },
        requestPart,
        image
    );

    assertThat(response.getDesignComponentId()).isNotNull();
    assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
    assertThat(response.getIsPublic()).isTrue();
    assertThat(response.getCreatedAt()).isNotNull();
    assertThat(response.getUpdatedAt()).isNotNull();
    assertThat(response.getComponentTypes())
        .extracting("componentTypeId")
        .containsExactlyInAnyOrder(componentTypeA.getComponentTypeId(),
            componentTypeB.getComponentTypeId());

    assertThat(designComponentRepository.count()).isEqualTo(1);
    DesignComponent saved = designComponentRepository.findByDesignComponentId(
        response.getDesignComponentId()).orElseThrow();
    assertThat(saved.getComponentTypes())
        .extracting(ComponentType::getComponentTypeId)
        .containsExactlyInAnyOrder(componentTypeA.getComponentTypeId(),
            componentTypeB.getComponentTypeId());
  }

  @Test
  @DisplayName("DesignComponent 다중 업로드 생성 테스트")
  void createDesignComponents_multiUpload() throws Exception {
    stubImageUpload();
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile file1 = multipartFixture.filesPart("test-1.png");
    MockMultipartFile file2 = multipartFixture.filesPart("test-2.png");

    List<DesignComponentDto> response = doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        200,
        new TypeReference<>() {
        },
        requestPart,
        file1,
        file2
    );

    assertThat(response).hasSize(2);
    assertThat(response)
        .extracting(DesignComponentDto::getPublicUserId)
        .containsOnly(testUser.getPublicUserId());
    assertThat(response).allSatisfy(asset -> assertThat(asset.getComponentTypes())
        .extracting("componentTypeId")
        .containsExactly(componentTypeA.getComponentTypeId()));

    assertThat(designComponentRepository.count()).isEqualTo(2);
  }

  @Test
  @DisplayName("files 1개 업로드도 정상 동작")
  void createDesignComponents_singleFileUpload() throws Exception {
    stubImageUpload();
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile file = multipartFixture.filesPart("single-file.png");

    List<DesignComponentDto> response = doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        200,
        new TypeReference<>() {
        },
        requestPart,
        file
    );

    assertThat(response).hasSize(1);
    assertThat(designComponentRepository.count()).isEqualTo(1);
  }

  @Test
  @DisplayName("다중 업로드 생성 시 여러 componentType 공유 가능")
  void createDesignComponents_multiUploadWithMultipleComponentTypes() throws Exception {
    stubImageUpload();
    CreateDesignComponentRequest createRequest = publicCreateRequest(componentTypeA, componentTypeB);
    MockMultipartFile requestPart = createRequestPart(createRequest);
    MockMultipartFile file1 = multipartFixture.filesPart("test-1.png");
    MockMultipartFile file2 = multipartFixture.filesPart("test-2.png");

    List<DesignComponentDto> response = doMultipartRequest(
        "/api/design-components/bulk",
        HttpMethod.POST,
        200,
        new TypeReference<>() {
        },
        requestPart,
        file1,
        file2
    );

    assertThat(response).hasSize(2);
    assertThat(response).allSatisfy(asset -> assertThat(asset.getComponentTypes())
        .extracting("componentTypeId")
        .containsExactlyInAnyOrder(componentTypeA.getComponentTypeId(),
            componentTypeB.getComponentTypeId()));

    assertThat(designComponentRepository.count()).isEqualTo(2);
  }
}
