package com.komentum.designcomponent.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.komentum.designcomponent.domain.DesignComponent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@DisplayName("DesignComponent 삭제 테스트")
class DesignComponentDeleteControllerTest extends DesignComponentControllerTestSupport {

  @Test
  @DisplayName("DesignComponent 삭제 테스트")
  void deleteDesignComponent() throws Exception {
    DesignComponent savedComponent = testUserComponent();

    MockHttpServletRequestBuilder requestBuilder =
        delete("/api/design-components/{id}", savedComponent.getDesignComponentId());

    mockMvc.perform(mockMvcUtils.addAuthentication(requestBuilder, testClient))
        .andExpect(status().isNoContent());

    assertThat(designComponentRepository.existsById(savedComponent.getDesignComponentId()))
        .isFalse();
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
  }
}
