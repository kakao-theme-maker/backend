package com.komentum.designcomponent.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.komentum.designcomponent.domain.DesignComponent;
import com.komentum.global.security.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@DisplayName("DesignComponent 삭제 테스트")
class DesignComponentDeleteControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("DesignComponent 삭제 테스트")
  void deleteDesignComponent() throws Exception {
    DesignComponent savedComponent = testUserComponent("image.png", true, componentTypeA);
    Integer componentId = savedComponent.getDesignComponentId();
    Integer componentTypeId = componentTypeA.getComponentTypeId();

    MockHttpServletRequestBuilder requestBuilder =
        delete("/api/design-components/{id}", savedComponent.getDesignComponentId());

    mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
        .andExpect(status().isNoContent());

    designComponentRepository.flush();
    entityManager.clear();
    DesignComponent retained = designComponentRepository.findByDesignComponentId(componentId)
        .orElseThrow();
    assertThat(retained.getIsActive()).isFalse();
    assertThat(retained.getFileName()).isEqualTo("image.png");
    assertThat(retained.getComponentTypes()).extracting("componentTypeId")
        .containsExactly(componentTypeId);
    verify(fileManager, never()).deleteFile(anyString());
  }

  @Test
  @DisplayName("다른 사용자의 DesignComponent 삭제 실패 테스트")
  void deleteDesignComponentByOtherUser() throws Exception {
    DesignComponent savedComponent = otherUserComponent(
        "other@test.com", "https://other.com/image.png", false, componentTypeA);

    MockHttpServletRequestBuilder requestBuilder =
        delete("/api/design-components/{id}", savedComponent.getDesignComponentId());

    mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
        .andExpect(status().isForbidden());

    assertThat(designComponentRepository.existsById(savedComponent.getDesignComponentId()))
        .isTrue();
    assertThat(savedComponent.getIsActive()).isTrue();
  }

  @Test
  @DisplayName("관리자는 다른 사용자의 DesignComponent를 비활성화할 수 있다")
  void deleteDesignComponentByAdmin() throws Exception {
    DesignComponent savedComponent = otherUserComponent(
        "other@test.com", "image.png", true, componentTypeA);
    authenticateAs(testUser, UserRole.ADMIN);

    performAuthenticated(delete("/api/design-components/{id}",
        savedComponent.getDesignComponentId()), status().isNoContent());

    designComponentRepository.flush();
    entityManager.clear();
    assertThat(designComponentRepository.findById(savedComponent.getDesignComponentId())
        .orElseThrow().getIsActive()).isFalse();
  }

  @Test
  @DisplayName("비활성 DesignComponent의 반복 삭제 요청도 204를 반환한다")
  void deleteInactiveDesignComponentAgain() throws Exception {
    DesignComponent savedComponent = testUserComponent();
    Integer componentId = savedComponent.getDesignComponentId();

    performAuthenticated(delete("/api/design-components/{id}", componentId),
        status().isNoContent());
    designComponentRepository.flush();
    entityManager.clear();
    performAuthenticated(delete("/api/design-components/{id}", componentId),
        status().isNoContent());

    assertThat(designComponentRepository.findById(componentId).orElseThrow().getIsActive())
        .isFalse();
  }

  @Test
  @DisplayName("비활성 DesignComponent도 다른 사용자의 삭제 요청은 거부한다")
  void deleteInactiveDesignComponentByOtherUser() throws Exception {
    DesignComponent savedComponent = otherUserComponent(
        "other@test.com", "image.png", true, componentTypeA);
    savedComponent.deactivate();
    designComponentRepository.flush();

    performAuthenticated(delete("/api/design-components/{id}",
        savedComponent.getDesignComponentId()), status().isForbidden());

    assertThat(savedComponent.getIsActive()).isFalse();
  }

  @Test
  @DisplayName("존재하지 않는 DesignComponent 삭제는 404를 반환한다")
  void deleteMissingDesignComponent() throws Exception {
    performAuthenticated(delete("/api/design-components/{id}", 999999), status().isNotFound());
  }
}
