package com.komentum.theme.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.type.TypeReference;
import com.komentum.theme.component.domain.ComponentType;
import com.komentum.theme.component.domain.DesignComponent;
import com.komentum.theme.component.dto.DesignComponentDto;
import com.komentum.theme.component.dto.UpdateDesignComponentRequest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;

@DisplayName("DesignComponent 수정 테스트")
class DesignComponentUpdateControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("DesignComponent 수정 테스트")
  void updateDesignComponent() throws Exception {
    DesignComponent savedComponent = testUserComponent(
        "http://example.com/image.png", false, componentTypeA);
    UpdateDesignComponentRequest updateRequest = updateRequest(true, componentTypeB);
    MockMultipartFile requestPart = createRequestPart(updateRequest);
    MockMultipartFile image = createImagePart("updated.png");

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
    assertThat(response.getImageUrl()).isNotBlank();
    assertThat(response.getImageUrl()).isNotEqualTo("http://example.com/image.png");
    assertThat(response.getImageUrl()).isEqualTo(UPLOADED_IMAGE_URL);
    assertThat(response.getCreatedAt()).isNotNull();
    assertThat(response.getUpdatedAt()).isNotNull();
    assertThat(response.getComponentTypes())
        .extracting("componentTypeId")
        .containsExactly(componentTypeB.getComponentTypeId());

    DesignComponent updated = designComponentRepository.findByDesignComponentId(
        savedComponent.getDesignComponentId()).orElseThrow();
    assertThat(updated.getComponentTypes())
        .extracting(ComponentType::getComponentTypeId)
        .containsExactly(componentTypeB.getComponentTypeId());
  }

  @Test
  @DisplayName("다른 사용자의 DesignComponent 수정 실패 테스트")
  void updateDesignComponentByOtherUser() throws Exception {
    DesignComponent savedComponent = otherUserComponent(
        "other@test.com", "http://example.com/image.png", false, componentTypeA);
    UpdateDesignComponentRequest updateRequest = updateRequest(true, componentTypeB);
    MockMultipartFile requestPart = createRequestPart(updateRequest);
    MockMultipartFile image = createImagePart("updated.png");

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
    assertThat(afterComponent.getImageUrl()).isEqualTo("http://example.com/image.png");
    assertThat(afterComponent.getIsPublic()).isFalse();
    assertThat(afterComponent.getComponentTypes())
        .extracting(ComponentType::getComponentTypeId)
        .containsExactly(componentTypeA.getComponentTypeId());
  }

  @Test
  @DisplayName("DesignComponent 수정 시 존재하지 않는 componentTypeId 검증")
  void updateDesignComponent_withUnknownComponentTypeId() throws Exception {
    DesignComponent savedComponent = testUserComponent(
        "http://example.com/image.png", false, componentTypeA);
    UpdateDesignComponentRequest updateRequest = updateRequest(true, List.of(999999));
    MockMultipartFile requestPart = createRequestPart(updateRequest);
    MockMultipartFile image = createImagePart("updated.png");

    doMultipartRequest(
        "/api/design-components/" + savedComponent.getDesignComponentId(),
        HttpMethod.PUT,
        404,
        new TypeReference<Void>() {
        },
        requestPart,
        image
    );

    verify(fileManager, never()).uploadFile(any(byte[].class), anyString());
  }
}
