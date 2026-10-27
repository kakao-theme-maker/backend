package com.komentum.designcomponent.controller;

import static com.komentum.test.fixture.component.DesignComponentRequestFixture.createRequestPart;
import static com.komentum.test.fixture.component.DesignComponentRequestFixture.imageUrlOf;
import static com.komentum.test.fixture.component.DesignComponentRequestFixture.updateRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.designcomponent.dto.DesignComponentDto;
import com.komentum.designcomponent.dto.UpdateDesignComponentRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("DesignComponent 수정 테스트")
class DesignComponentUpdateControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("타입 정보 없이 DesignComponent의 이미지와 공개 여부를 수정한다")
  void updateDesignComponent() throws Exception {
    DesignComponent savedComponent = testUserComponent(
        "image.png", false);
    UpdateDesignComponentRequest updateRequest = updateRequest(true);
    MockMultipartFile requestPart = createRequestPart(updateRequest);
    MockMultipartFile image = multipartFixture.imagePart("updated.png");

    DesignComponentDto response = doMultipartRequest(
        "/api/design-components/" + savedComponent.getDesignComponentId(),
        HttpMethod.PUT,
        200,
        new TypeReference<>() {
        },
        requestPart,
        image
    );

    assertThat(response.getDesignComponentId()).isEqualTo(savedComponent.getDesignComponentId());
    assertThat(response.getPublicUserId()).isEqualTo(testUser.getPublicUserId());
    assertThat(response.getIsPublic()).isTrue();
    ArgumentCaptor<String> fileNameCaptor = ArgumentCaptor.forClass(String.class);
    verify(fileManager).uploadFile(any(byte[].class), fileNameCaptor.capture());
    String uploadedFileName = fileNameCaptor.getValue();
    // 갱신된 이미지가 요청에 사용된 이미지의 URL과 동일한지 확인
    assertThat(response.getImageUrl()).isEqualTo(imageUrlOf(uploadedFileName));
    assertThat(response.getImageUrl()).isNotEqualTo(imageUrlOf("image.png"));
    assertThat(response.getCreatedAt()).isNotNull();
    assertThat(response.getUpdatedAt()).isNotNull();

    DesignComponent updated = designComponentRepository.findByDesignComponentId(
        savedComponent.getDesignComponentId()).orElseThrow();
    assertThat(updated.getFileName()).isEqualTo(uploadedFileName);
  }

  /**
   * 타입이나 이미지 없이 공개 여부만 수정할 수 있는지 검증한다.
   */
  @Test
  @DisplayName("이미지를 전달하지 않으면 파일을 유지하고 공개 여부만 수정한다")
  void updateDesignComponent_withoutImage() throws Exception {
    DesignComponent savedComponent = testUserComponent("image.png", false);

    DesignComponentDto response = doMultipartRequest(
        "/api/design-components/" + savedComponent.getDesignComponentId(),
        HttpMethod.PUT,
        200,
        new TypeReference<>() {
        },
        createRequestPart(updateRequest(true))
    );

    assertThat(response.getIsPublic()).isTrue();
    assertThat(response.getImageUrl()).isEqualTo(imageUrlOf("image.png"));
    assertThat(designComponentRepository.findById(savedComponent.getDesignComponentId())
        .orElseThrow().getFileName()).isEqualTo("image.png");
    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }

  @Test
  @DisplayName("다른 사용자의 DesignComponent 수정 실패 테스트")
  void updateDesignComponentByOtherUser() throws Exception {
    DesignComponent savedComponent = otherUserComponent(
        "other@test.com", "image.png", false);
    UpdateDesignComponentRequest updateRequest = updateRequest(true);
    MockMultipartFile requestPart = createRequestPart(updateRequest);
    MockMultipartFile image = multipartFixture.imagePart("updated.png");

    doMultipartRequest(
        "/api/design-components/" + savedComponent.getDesignComponentId(),
        HttpMethod.PUT,
        403,
        new TypeReference<Void>() {
        },
        requestPart,
        image
    );

    DesignComponent afterComponent = designComponentRepository.findById(
        savedComponent.getDesignComponentId()).orElseThrow();
    assertThat(afterComponent.getFileName()).isEqualTo("image.png");
    assertThat(afterComponent.getIsPublic()).isFalse();
  }

}
